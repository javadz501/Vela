package app.vela.ui.theme

import androidx.compose.ui.text.style.TextDirection
import org.junit.Assert.assertEquals
import org.junit.Test

class TypeDirectionTest {
    @Test
    fun allTypographyStylesUseRequestedRtlDirection() {
        val typography = velaTypography(direction = TextDirection.ContentOrRtl)
        val directions = listOf(
            typography.displayLarge.textDirection,
            typography.displayMedium.textDirection,
            typography.displaySmall.textDirection,
            typography.headlineLarge.textDirection,
            typography.headlineMedium.textDirection,
            typography.headlineSmall.textDirection,
            typography.titleLarge.textDirection,
            typography.titleMedium.textDirection,
            typography.titleSmall.textDirection,
            typography.bodyLarge.textDirection,
            typography.bodyMedium.textDirection,
            typography.bodySmall.textDirection,
            typography.labelLarge.textDirection,
            typography.labelMedium.textDirection,
            typography.labelSmall.textDirection,
        )

        directions.forEach { assertEquals(TextDirection.ContentOrRtl, it) }
    }

    @Test
    fun allTypographyStylesUseRequestedLtrDirection() {
        val typography = velaTypography(direction = TextDirection.ContentOrLtr)
        val directions = listOf(
            typography.displayLarge.textDirection,
            typography.displayMedium.textDirection,
            typography.displaySmall.textDirection,
            typography.headlineLarge.textDirection,
            typography.headlineMedium.textDirection,
            typography.headlineSmall.textDirection,
            typography.titleLarge.textDirection,
            typography.titleMedium.textDirection,
            typography.titleSmall.textDirection,
            typography.bodyLarge.textDirection,
            typography.bodyMedium.textDirection,
            typography.bodySmall.textDirection,
            typography.labelLarge.textDirection,
            typography.labelMedium.textDirection,
            typography.labelSmall.textDirection,
        )

        directions.forEach { assertEquals(TextDirection.ContentOrLtr, it) }
    }
}
