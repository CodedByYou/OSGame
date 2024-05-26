package me.codedbyyou.os.game.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.InputStream


/**
 * an image loader utility function that loads an image from the assets folder
 * @param fileName the name of the image file to load
 * @param size the size of the image
 * @param contentDescription the content description of the image
 */
@Composable
fun Image(fileName: String, size: Dp = 64.dp, contentDescription: String) {
    val imageBitmap =
        loadImageBitmap(object {}.javaClass.getResourceAsStream("/assets/$fileName")!!)

    Image(
        bitmap = imageBitmap,
        contentDescription = contentDescription,
        modifier = Modifier.size(size),
        contentScale = ContentScale.FillBounds
    )
}

/**
 * load profile image from the assets folder
 * @param fileName the name of the image file to load
 * @param size the size of the image
 * @param contentDescription the content description of the image
 * @param modifier the modifier of the image
 * @param elevation the elevation of the image
 * @param shadowColor the color of the shadow
 * @param backgroundColor the color of the background
 * @param isRound whether the image should be round or not
 * uses the ProfileImage composable with the default parameters
 */
@Composable
fun ProfileImage(
    fileName: String,
    size: Dp = 64.dp,
    contentDescription: String,
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    shadowColor: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    isRound: Boolean = true
) {
    ProfileImage(
        getResource(fileName),
        size,
        contentDescription,
        modifier,
        elevation,
        shadowColor,
        backgroundColor,
        isRound)
}


/**
 * load profile image from file
 * @param file the file of the image to load
 * @param size the size of the image
 * @param contentDescription the content description of the image
 * @param modifier the modifier of the image
 * @param elevation the elevation of the image
 * @param shadowColor the color of the shadow
 * @param backgroundColor the color of the background
 * @param isRound whether the image should be round or not
 */
@Composable
fun ProfileImage(
    file: File,
    size: Dp = 64.dp,
    contentDescription: String,
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    fallbackImage: String = "no-image.png",
    shadowColor: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    isRound: Boolean = true
) {
    if (!file.exists()){
        ProfileImage(
            fileName = fallbackImage,
            contentDescription = contentDescription,
            modifier = modifier,
            elevation = elevation,
            shadowColor = shadowColor,
            backgroundColor = backgroundColor,
            isRound = isRound
        )
        return
    }
   ProfileImage(file.inputStream(), size, contentDescription, modifier, elevation, shadowColor, backgroundColor, isRound)
}

/**
 * load profile image from input stream
 * @param file the file of the image to load
 * @param size the size of the image
 * @param contentDescription the content description of the image
 * @param modifier the modifier of the image
 * @param elevation the elevation of the image
 * @param shadowColor the color of the shadow
 * @param backgroundColor the color of the background
 * @param isRound whether the image should be round or not
 */
@Composable
fun ProfileImage(
    file: InputStream,
    size: Dp = 64.dp,
    contentDescription: String,
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    shadowColor: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    isRound: Boolean = true
) {
    val imageBitmap =
        loadImageBitmap(file)

    var modifierCpy = modifier
    modifierCpy = modifierCpy.size(size)
    modifierCpy = if (isRound) {
        modifierCpy
            .clip(CircleShape)
            .shadow(elevation, shape = CircleShape)
    } else {
        modifier
            .clip(RoundedCornerShape(8.dp))
            .shadow(elevation,
                shape = RoundedCornerShape(8.dp))
    }
    Image(
        bitmap = imageBitmap,
        contentDescription = contentDescription,
        modifier = modifierCpy,
        contentScale = ContentScale.FillBounds
    )
}

/**
 * an image loader utility function that loads an svg image from the assets folder
 * @param svgResource the name of the svg file to load
 * @param contentDescription the content description of the image
 */
@Composable
fun SvgImage(
    svgResource: String,
    contentDescription: String? = null,
    size: Dp = 64.dp
) {
    val svgVector = getSVGPainter(svgResource)
    Image(
        svgVector,
        contentDescription = contentDescription,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.size(size)
    )
}

/**
 * a helper function that returns a painter object from the svg file in the assets folder
 * @param svgResource the name of the svg file to load
 * @return a painter object of the svg file
 * @throws IllegalArgumentException if the resource is not found
 */
fun getSVGPainter(svgResource: String): Painter {
    val path: String = "/assets/$svgResource"
    val resourceStream = object {}.javaClass.getResourceAsStream(path)
        ?: throw IllegalArgumentException("Resource not found: $path")
    return loadSvgPainter(resourceStream, Density(1f, 1f))
}

/**
 * Loading Composable
 * @param modifier the modifier of the loading composable
 * @param color the color of the loading composable
 * @param size the size of the loading composable
 * @param strokeWidth the stroke width of the loading composable
 * @param strokeCap the stroke cap of the loading composable
 */
@Composable
fun Loading(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary, size: Dp = 64.dp, strokeWidth: Dp = 4.dp) {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = modifier.size(size).align(Alignment.Center),
            color = color,
            strokeWidth = strokeWidth
        )
    }
}


/**
 * a hoverable image composable
 * @param fileName the name of the image file to load
 * @param contentDescription the content description of the image
 * @param modifier the modifier of the image
 * @param isRound whether the image should be round or not
 * by default, the hover content is a semi-transparent overlay with an edit icon in the center
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun HoverableProfileImage(
    fileName: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    isRound: Boolean = false,
    onClick: () -> Unit = {},
    hoverContent: @Composable () -> Unit = {
        // Add a semi-transparent overlay to shade the image
        Box(
            modifier = Modifier.fillMaxSize()
                .clip(CircleShape)
                .background(Color(0, 0, 0, 128))
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Add your edit icon/button here
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
) {
    var isHovered by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.then(modifier)
            .pointerHoverIcon(PointerIcon.Hand,true)
            .onPointerEvent(PointerEventType.Move) {
            }
            .onPointerEvent(PointerEventType.Enter) {
                isHovered = true
            }
            .onPointerEvent(PointerEventType.Exit) {
                isHovered = false
            }.onClick { onClick() }
    ) {
        ProfileImage(
            fileName = fileName,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            isRound = isRound
        )
        if (isHovered) {
            hoverContent()
        }
    }
}


/**
 * a hoverable image composable
 * @param fileName the name of the image file to load
 * @param contentDescription the content description of the image
 * @param modifier the modifier of the image
 * @param isRound whether the image should be round or not
 * by default, the hover content is a semi-transparent overlay with an edit icon in the center
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun HoverableProfileImageWithFallBack(
    file: File,
    contentDescription: String,
    modifier: Modifier = Modifier,
    isRound: Boolean = false,
    onClick: () -> Unit = {},
    fallbackImage: String,
    hoverContent: @Composable () -> Unit = {
        // Add a semi-transparent overlay to shade the image
        Box(
            modifier = Modifier.fillMaxSize()
                .clip(if(isRound) CircleShape else RoundedCornerShape(8.dp))
                .background(Color(0, 0, 0, 128))
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Add your edit icon/button here
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
) {
    if (!file.exists()){
        HoverableProfileImage(
            fileName = fallbackImage,
            contentDescription = contentDescription,
            modifier = modifier,
            isRound = isRound,
            onClick = onClick,
            hoverContent = hoverContent
        )
        return
    }
    var isHovered by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.then(modifier)
            .pointerHoverIcon(PointerIcon.Hand,true)
            .onPointerEvent(PointerEventType.Move) {
            }
            .onPointerEvent(PointerEventType.Enter) {
                isHovered = true
            }
            .onPointerEvent(PointerEventType.Exit) {
                isHovered = false
            }.onClick { onClick() }
    ) {
        ProfileImage(
            file = file,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            isRound = isRound
        )
        if (isHovered) {
            hoverContent()
        }
    }
}

fun getResource(fileName: String): InputStream {
    return object {}.javaClass.getResourceAsStream("/assets/$fileName")!!
}

fun getAppResource(fileName: String): File {
    return File(System.getProperty("user.home")+File.separator+"qfa-referee-app"+File.separator+fileName)
}