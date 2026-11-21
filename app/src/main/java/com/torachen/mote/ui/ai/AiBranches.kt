package com.torachen.mote.ui.ai

/** 从根节点沿 selectedChildId 取当前可见分支；旧数据缺少关系时退回线性顺序。 */
internal fun aiVisiblePath(all: List<AiUiMessage>, rootId: Long?): List<AiUiMessage> {
    if (all.isEmpty()) return emptyList()
    val byId = all.associateBy { it.id }
    var cursor = rootId ?: all.firstOrNull()?.id
    val result = ArrayList<AiUiMessage>()
    val seen = HashSet<Long>()
    while (cursor != null && seen.add(cursor)) {
        val message = byId[cursor] ?: break
        result.add(message)
        cursor = message.selectedChildId
    }
    return if (result.isEmpty()) all else result
}

/** 同一父节点下的兄弟 id（含自己），按创建 id 排序；根消息的父节点视为 null。 */
internal fun aiVersionIds(all: List<AiUiMessage>, messageId: Long): List<Long> {
    val message = all.firstOrNull { it.id == messageId } ?: return emptyList()
    val parent = message.parentId
    return all
        .filter { if (parent == null) it.parentId == null else it.parentId == parent }
        .map { it.id }
        .sorted()
}
