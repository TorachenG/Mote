package com.torachen.mote.ui.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiMathTest {

    @Test
    fun `greek letters and operators`() {
        assertEquals("α + β ≤ γ", AiMath.convert("\\alpha + \\beta \\le \\gamma"))
        assertEquals("x × y ÷ z", AiMath.convert("x \\times y \\div z"))
        assertEquals("a ∈ A ∪ B", AiMath.convert("a \\in A \\cup B"))
    }

    @Test
    fun `fractions`() {
        assertEquals("a/b", AiMath.convert("\\frac{a}{b}"))
        assertEquals("(a+b)/2", AiMath.convert("\\frac{a+b}{2}"))
    }

    @Test
    fun `superscripts and subscripts`() {
        assertEquals("x²", AiMath.convert("x^2"))
        assertEquals("a₁", AiMath.convert("a_1"))
        assertEquals("∑ᵢ₌₁ⁿ i", AiMath.convert("\\sum_{i=1}^{n} i"))
        assertEquals("∫₀¹ x² dx", AiMath.convert("\\int_0^1 x^2 dx"))
    }

    @Test
    fun `sqrt and text`() {
        assertEquals("√(x+1)", AiMath.convert("\\sqrt{x+1}"))
        assertEquals("速度", AiMath.convert("\\text{速度}"))
        assertEquals("ℝ", AiMath.convert("\\mathbb{R}"))
    }

    @Test
    fun `unknown commands keep readable names`() {
        assertEquals("foo", AiMath.convert("\\foo"))
    }

    @Test
    fun `looks like math`() {
        assertTrue(AiMath.looksLikeMath("x^2"))
        assertTrue(AiMath.looksLikeMath("\\frac{1}{2}"))
        assertTrue(AiMath.looksLikeMath("5+3"))
        assertFalse(AiMath.looksLikeMath("5"))
        assertFalse(AiMath.looksLikeMath(" 5"))
        assertFalse(AiMath.looksLikeMath("5 和 "))
        assertFalse(AiMath.looksLikeMath("a\nb"))
        assertFalse(AiMath.looksLikeMath("https://example.com"))
    }
}
