import com.chaeyoon.lightmeter.LightMeterCalculator
import junit.framework.TestCase.assertEquals
import org.junit.Test
import kotlin.collections.flatMap
import kotlin.collections.flatMapIndexed
import kotlin.collections.flatten
import kotlin.collections.map
import kotlin.collections.mapNotNull
import kotlin.collections.minByOrNull
import kotlin.collections.zip
import kotlin.math.abs
import kotlin.math.roundToInt


class LightMeterCalculatorTest {
    private val testInstance = LightMeterCalculator()

    private val shutterSpeedValues = listOf(
        1f,
        1f / 2,
        1f / 4,
        1f / 8,
        1f / 15,
        1f / 30,
        1f / 60,
        1f / 125,
        1f / 250,
        1f / 500,
        1f / 1000
    )
    private val apertureValues = listOf(1f, 1.4f, 2f, 2.8f, 4f, 5.6f, 8f, 11f, 16f, 22f)
    private val isoValues = listOf(25f, 100f, 200f, 400f, 800f, 1600f, 3200f)
    private val exposureValues = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    @Test
    fun testEvCalculate() {
        val expectedExposureValues = apertureValues
            .flatMapIndexed { index, _ ->
                exposureValues.map { it + index }
            }

        val resultExposureValues = apertureValues.flatMap { aperture ->
            shutterSpeedValues
                .mapNotNull { shutterSpeed ->
                    try {
                        testInstance.calculateExposureValue(
                            aperture,
                            shutterSpeed,
                            100f
                        )
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }.map { it.roundToInt() }
        }

        assertEquals(expectedExposureValues, resultExposureValues)
    }

    @Test
    fun testShutterSpeedCalculate() {
        val expectedShutterSpeedValues = List(apertureValues.size) { shutterSpeedValues }.flatten()
        val resultShutterSpeedValues = apertureValues.flatMapIndexed { index, aperture ->
            exposureValues.mapNotNull { exposureValue ->
                try {
                    testInstance.calculateShutterSpeedValue(
                        (exposureValue + index).toFloat(),
                        100f,
                        aperture
                    )
                } catch (e: IllegalArgumentException) {
                    null
                }
            }.map {
                it.nearest(shutterSpeedValues)
            }
        }

        assertEquals(expectedShutterSpeedValues, resultShutterSpeedValues)
    }

    @Test
    fun testIsoCalculate() {
        val expectedIsoValues = List(apertureValues.size * shutterSpeedValues.size) { 100 }
        val resultIsoValues = apertureValues.flatMapIndexed { index, aperture ->
            shutterSpeedValues.zip(exposureValues).mapNotNull { (shutterSpeed, exposureValue) ->
                try {
                    testInstance.calculateIsoValue(
                        (exposureValue + index).toFloat(),
                        shutterSpeed,
                        aperture
                    )
                } catch (e: IllegalArgumentException) {
                    null
                }
            }.map { it.nearest(isoValues).toInt() }
        }

        assertEquals(expectedIsoValues, resultIsoValues)
    }

    @Test
    fun testApertureCalculate() {
        val expectedApertureValues = apertureValues.flatMap { aperture ->
            List(shutterSpeedValues.size) { aperture }
        }
        val resultApertureValues = apertureValues.flatMapIndexed { index, _ ->
            shutterSpeedValues.zip(exposureValues).mapNotNull { (shutterSpeed, exposureValue) ->
                try {
                    testInstance.calculateApertureValue(
                        ev = (exposureValue + index).toFloat(),
                        iso = 100f,
                        shutterSpeed = shutterSpeed
                    )
                } catch (e: IllegalArgumentException) {
                    null
                }
            }.map { it.nearest(apertureValues) }
        }
        assertEquals(expectedApertureValues, resultApertureValues)
    }

    private fun Float.nearest(among: List<Float>): Float {
        return among.minByOrNull { abs(it - this) } ?: this
    }
}