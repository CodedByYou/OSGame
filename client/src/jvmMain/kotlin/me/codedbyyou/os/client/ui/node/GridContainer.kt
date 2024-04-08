package me.codedbyyou.os.client.ui.node

import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.node
import com.lehaine.littlekt.graph.node.ui.Container
import com.lehaine.littlekt.graph.node.ui.Control
import kotlin.math.ceil

fun Node.gridContainer(callback: GridContainer.() -> Unit = {}, ) = node(GridContainer(), callback)

class GridContainer(

) : Container() {

    private var cellWidth = 0f
    private var cellHeight = 0f
    private var columns = 0
    private var paddingX = 0f
    private var paddingY = 0f

    fun setGridProperties(
        columns: Int,
        cellWidth: Float,
        cellHeight: Float,
        paddingX: Float = 0f,
        paddingY: Float = 0f
    ) {
        this.columns = columns
        this.cellWidth = cellWidth
        this.cellHeight = cellHeight
        this.paddingX = paddingX
        this.paddingY = paddingY
    }

    override fun onSortChildren() {
        var column = 0
        var row = 0
        val totalChildren = nodes.size
        for (i in 0 until totalChildren) {
            val child = nodes[i]
            if (child is Control && child.enabled) {
                val newX = paddingX + column * (cellWidth + paddingX)
                val newY = paddingY + row * (cellHeight + paddingY)
                fitChild(child, newX, newY, cellWidth, cellHeight)
                column++
                if (column >= columns) {
                    column = 0
                    row++
                }
            }
        }
    }

    override fun calculateMinSize() {
        if (!minSizeInvalid) return

        var maxContentWidth = 0f
        var maxContentHeight = 0f

        nodes.forEach {
            if (it is Control && it.enabled) {
                if (it.combinedMinWidth > maxContentWidth) {
                    maxContentWidth = it.combinedMinWidth
                }
                if (it.combinedMinHeight > maxContentHeight) {
                    maxContentHeight = it.combinedMinHeight
                }
            }
        }

        _internalMinWidth = (maxContentWidth + paddingX) * columns - paddingX
        _internalMinHeight = (maxContentHeight + paddingY) * (ceil(nodes.size.toFloat() / columns.toFloat())) - paddingY
        minSizeInvalid = false
    }
}
