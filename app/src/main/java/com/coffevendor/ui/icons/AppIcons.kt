package com.coffevendor.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.Color

object AppIcons {
    val ArrowDropDown: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArrowDropDown",
            defaultWidth = androidx.compose.ui.unit.Dp.Unspecified,
            defaultHeight = androidx.compose.ui.unit.Dp.Unspecified,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = androidx.compose.ui.graphics.SolidColor(Color.Black)
            ) {
                moveTo(7.41f, 8.59f)
                lineTo(12f, 13.17f)
                lineTo(16.59f, 8.59f)
                lineTo(18f, 10f)
                lineTo(12f, 16f)
                lineTo(6f, 10f)
                close()
            }
        }.build()
    }

    val Fingerprint: ImageVector by lazy {
        ImageVector.Builder(
            name = "Fingerprint",
            defaultWidth = androidx.compose.ui.unit.Dp.Unspecified,
            defaultHeight = androidx.compose.ui.unit.Dp.Unspecified,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = androidx.compose.ui.graphics.SolidColor(Color.Black)
            ) {
                moveTo(17.81f, 4.47f)
                curveTo(17.73f, 4.47f, 17.65f, 4.45f, 17.58f, 4.41f)
                curveTo(15.66f, 3.42f, 14.0f, 3.0f, 12.01f, 3.0f)
                curveTo(10.03f, 3.0f, 8.15f, 3.41f, 6.29f, 4.41f)
                curveTo(5.99f, 4.57f, 5.65f, 4.46f, 5.48f, 4.17f)
                curveTo(5.32f, 3.87f, 5.43f, 3.51f, 5.72f, 3.35f)
                curveTo(7.78f, 2.29f, 9.98f, 1.85f, 12.01f, 1.85f)
                curveTo(14.03f, 1.85f, 16.11f, 2.28f, 18.15f, 3.34f)
                curveTo(18.44f, 3.49f, 18.57f, 3.85f, 18.42f, 4.15f)
                curveTo(18.29f, 4.39f, 18.06f, 4.47f, 17.81f, 4.47f)
                close()
                moveTo(3.5f, 9.72f)
                curveTo(3.4f, 9.72f, 3.3f, 9.69f, 3.21f, 9.63f)
                curveTo(3.0f, 9.48f, 2.93f, 9.18f, 3.08f, 8.97f)
                curveTo(4.09f, 7.57f, 5.7f, 6.66f, 7.42f, 6.29f)
                curveTo(7.76f, 6.22f, 8.11f, 6.42f, 8.18f, 6.76f)
                curveTo(8.25f, 7.1f, 8.05f, 7.45f, 7.71f, 7.52f)
                curveTo(6.25f, 7.84f, 4.89f, 8.61f, 3.95f, 9.97f)
                curveTo(3.8f, 10.18f, 3.6f, 10.22f, 3.5f, 9.72f)
                close()
                moveTo(12f, 21.5f)
                curveTo(10.78f, 21.5f, 9.6f, 21.13f, 8.55f, 20.45f)
                curveTo(8.25f, 20.26f, 8.18f, 19.86f, 8.38f, 19.57f)
                curveTo(8.57f, 19.27f, 8.97f, 19.2f, 9.26f, 19.39f)
                curveTo(10.11f, 19.93f, 11.04f, 20.22f, 12f, 20.22f)
                curveTo(15.21f, 20.22f, 17.85f, 17.58f, 17.85f, 14.37f)
                curveTo(17.85f, 14.07f, 18.09f, 13.83f, 18.39f, 13.83f)
                curveTo(18.69f, 13.83f, 18.93f, 14.07f, 18.93f, 14.37f)
                curveTo(18.93f, 18.18f, 15.85f, 21.32f, 12f, 21.32f)
                curveTo(11.06f, 21.32f, 10.16f, 21.13f, 9.33f, 20.78f)
                curveTo(9.03f, 20.65f, 8.69f, 20.79f, 8.56f, 21.09f)
                curveTo(8.43f, 21.39f, 8.57f, 21.73f, 8.87f, 21.86f)
                curveTo(9.91f, 22.31f, 10.94f, 22.5f, 12f, 22.5f)
                curveTo(16.56f, 22.5f, 20.17f, 18.94f, 20.17f, 14.37f)
                curveTo(20.17f, 13.39f, 19.36f, 12.58f, 18.39f, 12.58f)
                curveTo(17.41f, 12.58f, 16.6f, 13.39f, 16.6f, 14.37f)
                curveTo(16.6f, 16.89f, 14.5f, 18.99f, 12f, 18.99f)
                curveTo(9.79f, 18.99f, 7.9f, 17.27f, 7.65f, 15.09f)
                curveTo(7.62f, 14.83f, 7.78f, 14.58f, 8.04f, 14.55f)
                curveTo(8.3f, 14.52f, 8.55f, 14.68f, 8.58f, 14.94f)
                curveTo(8.78f, 16.81f, 10.31f, 17.82f, 12f, 17.82f)
                curveTo(14.03f, 17.82f, 15.65f, 16.2f, 15.65f, 14.17f)
                curveTo(15.65f, 12.75f, 16.55f, 11.85f, 17.97f, 11.85f)
                curveTo(19.39f, 11.85f, 20.29f, 12.75f, 20.29f, 14.17f)
                curveTo(20.29f, 18.58f, 16.67f, 22.2f, 12.26f, 22.48f)
                curveTo(12.17f, 22.49f, 12.09f, 22.5f, 12f, 22.5f)
                curveTo(8.13f, 22.5f, 4.58f, 19.64f, 3.62f, 15.73f)
                curveTo(3.55f, 15.47f, 3.7f, 15.2f, 3.96f, 15.13f)
                curveTo(4.22f, 15.06f, 4.49f, 15.21f, 4.56f, 15.47f)
                curveTo(5.37f, 18.86f, 8.39f, 21.32f, 12f, 21.32f)
                curveTo(12.09f, 21.32f, 12.17f, 21.32f, 12.26f, 21.31f)
                close()
                moveTo(12.01f, 7.52f)
                curveTo(10.13f, 7.52f, 8.49f, 8.41f, 7.35f, 9.89f)
                curveTo(7.19f, 10.1f, 7.22f, 10.38f, 7.43f, 10.55f)
                curveTo(7.63f, 10.71f, 7.92f, 10.68f, 8.09f, 10.48f)
                curveTo(9.05f, 9.22f, 10.47f, 8.44f, 12.01f, 8.44f)
                curveTo(13.55f, 8.44f, 14.97f, 9.22f, 15.93f, 10.48f)
                curveTo(16.1f, 10.68f, 16.39f, 10.71f, 16.59f, 10.55f)
                curveTo(16.8f, 10.38f, 16.83f, 10.1f, 16.67f, 9.89f)
                curveTo(15.53f, 8.41f, 13.89f, 7.52f, 12.01f, 7.52f)
                close()
            }
        }.build()
    }

    val LocalCafe: ImageVector by lazy {
        ImageVector.Builder(
            name = "LocalCafe",
            defaultWidth = androidx.compose.ui.unit.Dp.Unspecified,
            defaultHeight = androidx.compose.ui.unit.Dp.Unspecified,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = androidx.compose.ui.graphics.SolidColor(Color.Black)
            ) {
                moveTo(20.0f, 3.0f)
                lineTo(2.0f, 3.0f)
                curveTo(0.9f, 3.0f, 0.0f, 3.9f, 0.0f, 5.0f)
                lineTo(0.0f, 14.0f)
                curveTo(0.0f, 15.1f, 0.9f, 16.0f, 2.0f, 16.0f)
                lineTo(6.0f, 16.0f)
                lineTo(6.0f, 19.0f)
                lineTo(14.0f, 19.0f)
                lineTo(14.0f, 16.0f)
                lineTo(18.0f, 16.0f)
                curveTo(19.1f, 16.0f, 20.0f, 15.1f, 20.0f, 14.0f)
                lineTo(20.0f, 5.0f)
                curveTo(20.0f, 3.9f, 19.1f, 3.0f, 20.0f, 3.0f)
                close()
                moveTo(18.0f, 14.0f)
                lineTo(4.0f, 14.0f)
                lineTo(4.0f, 5.0f)
                lineTo(18.0f, 5.0f)
                close()
            }
        }.build()
    }

    val Schedule: ImageVector by lazy {
        ImageVector.Builder(
            name = "Schedule",
            defaultWidth = androidx.compose.ui.unit.Dp.Unspecified,
            defaultHeight = androidx.compose.ui.unit.Dp.Unspecified,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = androidx.compose.ui.graphics.SolidColor(Color.Black)
            ) {
                moveTo(11.99f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                curveTo(2.0f, 17.52f, 6.47f, 22.0f, 11.99f, 22.0f)
                curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                curveTo(22.0f, 6.48f, 17.52f, 2.0f, 11.99f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveTo(7.58f, 20.0f, 4.0f, 16.42f, 4.0f, 12.0f)
                curveTo(4.0f, 7.58f, 7.58f, 4.0f, 12.0f, 4.0f)
                curveTo(16.42f, 4.0f, 20.0f, 7.58f, 20.0f, 12.0f)
                curveTo(20.0f, 16.42f, 16.42f, 20.0f, 12.0f, 20.0f)
                close()
                moveTo(12.5f, 7.0f)
                lineTo(11.0f, 7.0f)
                lineTo(11.0f, 13.0f)
                lineTo(16.25f, 16.15f)
                lineTo(17.01f, 14.89f)
                lineTo(12.5f, 12.25f)
                close()
            }
        }.build()
    }

    val Logout: ImageVector by lazy {
        ImageVector.Builder(
            name = "Logout",
            defaultWidth = androidx.compose.ui.unit.Dp.Unspecified,
            defaultHeight = androidx.compose.ui.unit.Dp.Unspecified,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = androidx.compose.ui.graphics.SolidColor(Color.Black)
            ) {
                moveTo(17.0f, 7.0f)
                lineTo(15.59f, 8.41f)
                lineTo(18.17f, 11.0f)
                lineTo(6.0f, 11.0f)
                lineTo(6.0f, 13.0f)
                lineTo(18.17f, 13.0f)
                lineTo(15.59f, 15.59f)
                lineTo(17.0f, 17.0f)
                lineTo(22.0f, 12.0f)
                close()
            }
            path(
                fill = androidx.compose.ui.graphics.SolidColor(Color.Black)
            ) {
                moveTo(4.0f, 5.0f)
                lineTo(4.0f, 19.0f)
                lineTo(12.0f, 19.0f)
                lineTo(12.0f, 17.0f)
                lineTo(6.0f, 17.0f)
                lineTo(6.0f, 7.0f)
                lineTo(12.0f, 7.0f)
                lineTo(12.0f, 5.0f)
                close()
            }
        }.build()
    }
}
