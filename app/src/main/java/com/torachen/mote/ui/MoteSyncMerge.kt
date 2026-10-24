package com.torachen.mote.ui

import org.json.JSONArray
import org.json.JSONObject

/**
 * 同步数据的三方合并：本地 / 云端 / 上次同步快照。
 *
 * 规则：
 * - 两端新增都保留（并集）；
 * - 上次同步之后，任意一端删除的条目，合并结果里也删除；
 * - 没有快照（新设备首次同步）时只做并集，绝不删除已有内容；
 * - 只从云端来的浏览历史会带 isRemote = true，本机访问过的会被改回 false。
 */
object MoteSyncMerge {

    const val SNAPSHOT_KEY = "sync_snapshot_v1"

    private val ARRAY_KEYS = listOf(
        "bookmarks_v2", "history", "home_quick_urls", "saved_passwords_v1"
    )

    fun merge(local: JSONObject, remote: JSONObject, snapshot: JSONObject): JSONObject {
        val out = JSONObject()

        val fallback = when {
            local.has("fallback_key") -> local.optString("fallback_key", "")
            remote.has("fallback_key") -> remote.optString("fallback_key", "")
            else -> null
        }
        if (fallback != null) out.put("fallback_key", fallback)

        mergeBookmarks(local, remote, snapshot, out)
        mergeHistory(local, remote, snapshot, out)
        mergeKeyedArray("home_quick_urls", local, remote, snapshot, out, ::quickUrlKey)
        mergeKeyedArray("saved_passwords_v1", local, remote, snapshot, out, ::passwordKey)
        mergeSearchHistory(local, remote, snapshot, out)
        mergeSettings(local, remote, snapshot, out)

        return out
    }

    /* ---------------- 工具 ---------------- */

    /** 某一端没有这个同步项（通常是本地关掉了这个类别）：原样保留另一端的数据 */
    private fun keepOneSide(name: String, local: JSONObject, remote: JSONObject, out: JSONObject): Boolean {
        val hasLocal = local.has(name)
        val hasRemote = remote.has(name)
        if (hasLocal && hasRemote) return false
        if (hasLocal) out.put(name, local.opt(name))
        if (hasRemote) out.put(name, remote.opt(name))
        return true
    }

    private fun parseArray(source: JSONObject, name: String): JSONArray? {
        if (!source.has(name)) return null
        val raw = source.optString(name, "")
        if (raw.isBlank()) return null
        return try {
            JSONArray(raw)
        } catch (_: Exception) {
            null
        }
    }

    private fun putArray(out: JSONObject, name: String, arr: JSONArray) {
        out.put(name, arr.toString())
    }

    /* ---------------- 书签（树） ---------------- */

    private class FlatNode(val obj: JSONObject, val parentId: String)

    private fun flattenBookmarks(arr: JSONArray?, parentId: String, out: MutableMap<String, FlatNode>) {
        arr ?: return
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val id = obj.optString("id", "")
            if (id.isBlank()) continue
            out[id] = FlatNode(JSONObject(obj.toString()), parentId)
            if (obj.optBoolean("isFolder", false)) {
                flattenBookmarks(obj.optJSONArray("children"), id, out)
            }
        }
    }

    private fun buildBookmarkTree(items: Map<String, FlatNode>): JSONArray {
        val childrenByParent = items.values.groupBy { it.parentId }
        val rootNodes = ArrayList<FlatNode>()
        rootNodes.addAll(childrenByParent[""].orEmpty())
        // 父文件夹已被删除的节点挂到根目录，避免丢失
        for ((parent, list) in childrenByParent) {
            if (parent.isNotEmpty() && !items.containsKey(parent)) rootNodes.addAll(list)
        }

        fun build(nodes: List<FlatNode>): JSONArray {
            val arr = JSONArray()
            for (node in nodes) {
                val copy = JSONObject()
                copy.put("id", node.obj.optString("id"))
                copy.put("title", node.obj.optString("title", ""))
                val url = node.obj.optString("url", "")
                if (url.isNotBlank()) copy.put("url", url)
                copy.put("isFolder", node.obj.optBoolean("isFolder", false))
                copy.put("time", node.obj.optLong("time", 0L))
                if (copy.optBoolean("isFolder", false)) {
                    copy.put("children", build(childrenByParent[copy.optString("id")].orEmpty()))
                }
                arr.put(copy)
            }
            return arr
        }

        return build(rootNodes)
    }

    private fun mergeBookmarks(
        local: JSONObject,
        remote: JSONObject,
        snapshot: JSONObject,
        out: JSONObject,
    ) {
        if (keepOneSide("bookmarks_v2", local, remote, out)) return
        val localArr = parseArray(local, "bookmarks_v2")
        val remoteArr = parseArray(remote, "bookmarks_v2")
        if (localArr == null && remoteArr == null) return

        val localMap = LinkedHashMap<String, FlatNode>()
        val remoteMap = LinkedHashMap<String, FlatNode>()
        val prevMap = LinkedHashMap<String, FlatNode>()
        flattenBookmarks(localArr, "", localMap)
        flattenBookmarks(remoteArr, "", remoteMap)
        flattenBookmarks(parseArray(snapshot, "bookmarks_v2"), "", prevMap)

        val deleted = HashSet<String>()
        for (id in prevMap.keys) {
            if (!localMap.containsKey(id) || !remoteMap.containsKey(id)) deleted.add(id)
        }

        val merged = LinkedHashMap<String, FlatNode>()
        for ((id, node) in localMap) if (id !in deleted) merged[id] = node
        for ((id, node) in remoteMap) {
            if (id in deleted || merged.containsKey(id)) continue
            merged[id] = node
        }

        putArray(out, "bookmarks_v2", buildBookmarkTree(merged))
    }

    /* ---------------- 浏览历史 ---------------- */

    private fun mergeHistory(
        local: JSONObject,
        remote: JSONObject,
        snapshot: JSONObject,
        out: JSONObject,
    ) {
        if (keepOneSide("history", local, remote, out)) return
        val localArr = parseArray(local, "history")
        val remoteArr = parseArray(remote, "history")
        if (localArr == null && remoteArr == null) return

        fun keyOf(obj: JSONObject): String = obj.optString("url", "")

        val localMap = LinkedHashMap<String, JSONObject>()
        localArr?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                localMap[keyOf(obj)] = obj
            }
        }
        val remoteMap = LinkedHashMap<String, JSONObject>()
        remoteArr?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                remoteMap[keyOf(obj)] = obj
            }
        }
        val prevKeys = parseArray(snapshot, "history")
            ?.let { arr -> (0 until arr.length()).mapNotNull { arr.optJSONObject(it)?.let(::keyOf) } }
            ?.toSet() ?: emptySet()

        val deleted = prevKeys.filter { it !in localMap.keys || it !in remoteMap.keys }.toSet()

        val merged = LinkedHashMap<String, JSONObject>()
        for ((key, value) in localMap) if (key !in deleted) merged[key] = value
        for ((key, value) in remoteMap) {
            if (key in deleted) continue
            val existing = merged[key]
            if (existing == null) {
                merged[key] = JSONObject(value.toString()).apply { put("isRemote", true) }
            } else {
                val localTime = existing.optLong("time", 0L)
                if (value.optLong("time", 0L) > localTime) {
                    merged[key] = JSONObject(value.toString()).apply {
                        put("isRemote", existing.optBoolean("isRemote", false))
                    }
                }
            }
        }

        val arr = JSONArray()
        for (value in merged.values) arr.put(value)
        putArray(out, "history", arr)
    }

    /* ---------------- 通用数组（快捷网址 / 密码） ---------------- */

    private fun quickUrlKey(obj: JSONObject): String {
        val id = obj.optString("id", "")
        return if (id.isNotBlank()) id else "u:" + obj.optString("url", "")
    }

    private fun passwordKey(obj: JSONObject): String {
        val id = obj.optString("id", "")
        return if (id.isNotBlank()) id else "u:" + obj.optString("url", "") + "|" + obj.optString("username", "")
    }

    private fun mergeKeyedArray(
        name: String,
        local: JSONObject,
        remote: JSONObject,
        snapshot: JSONObject,
        out: JSONObject,
        keyOf: (JSONObject) -> String,
    ) {
        if (keepOneSide(name, local, remote, out)) return
        val localArr = parseArray(local, name)
        val remoteArr = parseArray(remote, name)
        if (localArr == null && remoteArr == null) return

        val localMap = LinkedHashMap<String, JSONObject>()
        localArr?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                localMap[keyOf(obj)] = obj
            }
        }
        val remoteMap = LinkedHashMap<String, JSONObject>()
        remoteArr?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                remoteMap[keyOf(obj)] = obj
            }
        }
        val prevKeys = parseArray(snapshot, name)
            ?.let { arr -> (0 until arr.length()).mapNotNull { arr.optJSONObject(it)?.let(keyOf) } }
            ?.toSet() ?: emptySet()

        val deleted = prevKeys.filter { it !in localMap.keys || it !in remoteMap.keys }.toSet()

        val merged = LinkedHashMap<String, JSONObject>()
        for ((key, value) in localMap) if (key !in deleted) merged[key] = value
        for ((key, value) in remoteMap) {
            if (key in deleted) continue
            if (!merged.containsKey(key)) merged[key] = value
        }

        val arr = JSONArray()
        for (value in merged.values) arr.put(value)
        putArray(out, name, arr)
    }

    /* ---------------- 搜索历史（"|||" 分隔） ---------------- */

    private fun parseSearch(source: JSONObject): List<String>? {
        if (!source.has("search_history")) return null
        return source.optString("search_history", "").split("|||").filter { it.isNotBlank() }
    }

    private fun mergeSearchHistory(
        local: JSONObject,
        remote: JSONObject,
        snapshot: JSONObject,
        out: JSONObject,
    ) {
        if (keepOneSide("search_history", local, remote, out)) return
        val localList = parseSearch(local) ?: return
        val remoteList = parseSearch(remote) ?: run {
            out.put("search_history", localList.joinToString("|||"))
            return
        }

        val prevSet = parseSearch(snapshot)?.toSet() ?: emptySet()
        val localSet = localList.toSet()
        val remoteSet = remoteList.toSet()
        val deleted = prevSet.filter { it !in localSet || it !in remoteSet }.toSet()

        val merged = LinkedHashSet<String>()
        for (item in localList) if (item !in deleted) merged.add(item)
        for (item in remoteList) if (item !in deleted) merged.add(item)

        out.put("search_history", merged.joinToString("|||"))
    }

    /* ---------------- 设置项 ---------------- */

    private fun mergeSettings(
        local: JSONObject,
        remote: JSONObject,
        snapshot: JSONObject,
        out: JSONObject,
    ) {
        if (keepOneSide("settings", local, remote, out)) return
        val localObj = local.optJSONObject("settings") ?: return
        val remoteObj = remote.optJSONObject("settings") ?: run {
            out.put("settings", localObj)
            return
        }
        val prevObj = snapshot.optJSONObject("settings")

        fun keysOf(obj: JSONObject?): Set<String> =
            obj?.keys()?.asSequence()?.toSet() ?: emptySet()

        val localKeys = keysOf(localObj)
        val remoteKeys = keysOf(remoteObj)
        val prevKeys = keysOf(prevObj)

        val result = JSONObject()
        for (key in localKeys) {
            val removedLocally = key in prevKeys && key !in localKeys
            val removedRemotely = key in prevKeys && key !in remoteKeys
            if (removedLocally || removedRemotely) continue
            result.put(key, localObj.opt(key))
        }
        for (key in remoteKeys) {
            if (result.has(key)) continue
            val removedRemotely = key in prevKeys && key !in localKeys
            if (removedRemotely) continue
            result.put(key, remoteObj.opt(key))
        }

        out.put("settings", result)

        // 类型表跟着设置项一起走，避免 long 被当成 int 写回
        val types = JSONObject()
        val localTypes = local.optJSONObject("settings_types")
        val remoteTypes = remote.optJSONObject("settings_types")
        for (key in result.keys().asSequence().toList()) {
            val type = localTypes?.optString(key, "")?.takeIf { it.isNotEmpty() }
                ?: remoteTypes?.optString(key, "")?.takeIf { it.isNotEmpty() }
            if (type != null) types.put(key, type)
        }
        out.put("settings_types", types)
    }
}
