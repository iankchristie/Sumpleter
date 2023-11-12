import SumpleteBoard.State.*

data class SumpleteBoard(val entries: List<List<BoardEntry>>, val rowSums: List<Int>, val colSums: List<Int>) : Solvable {
    fun size(): Int {
        return entries.size
    }

    override fun isSolved(): Boolean {
        return entries.matrixAll { it.state != UNKNOWN }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        var rowLength = 0
        entries.forEachIndexed { rowNum, row ->
            val rowString = row.joinToString("\t") + "\t| ${rowSums[rowNum]}"
            rowLength = maxOf(rowLength, rowString.length)
            builder.appendLine(rowString)
        }
        builder.appendLine('-'.repeat(rowLength))
        builder.appendLine(colSums.joinToString("\t"))

        return builder.toString()
    }

    companion object {
        fun generate(boardSize: Int): SumpleteBoard {
            val matrix = Array(boardSize) { Array(boardSize) { (1..10).random() }.asList() }.asList()
            val bitMap = Array(boardSize) { Array(boardSize) { (0..1).random() }.asList() }.asList()

            val combinedMap = matrix.matrixMapIndexed { row, col, entry ->
                entry * bitMap[row][col]
            }

            val rowSums = combinedMap.matrixMapRows { it.sum() }
            val colSums = combinedMap.matrixMapColumns { it.sum() }

            val entries = matrix.matrixMap {
                BoardEntry(it, UNKNOWN)
            }

            return SumpleteBoard(entries, rowSums, colSums)
        }

        fun generateFromBoard(): SumpleteBoard {
            val matrix = listOf(
                listOf(8, 8, 6, 4, 8, 4, 3),
                listOf(4, 5, 7, 4, 1, 4, 7),
                listOf(4, 8, 1, 2, 5, 8, 6),
                listOf(5, 6, 3, 7, 8, 7, 2),
                listOf(8, 1, 4, 3, 6, 4, 1),
                listOf(2, 7, 3, 9, 6, 3, 2),
                listOf(9, 5, 6, 4, 9, 8, 3),
            )

            val rowSums = listOf(19, 18, 13, 23, 12, 16, 23)
            val colSums = listOf(29, 20, 17, 12, 17, 15, 14)

            val entries = matrix.matrixMap {
                BoardEntry(it, UNKNOWN)
            }

            return SumpleteBoard(entries, rowSums, colSums)
        }
    }

    data class BoardEntry(val value: Int, val state: State) {
        override fun toString(): String {
            return when (state) {
                INCLUDED -> "${value}*"
                EXCLUDED -> "${value}X"
                UNKNOWN -> "$value"
            }
        }
    }

    enum class State {
        INCLUDED, EXCLUDED, UNKNOWN
    }
}

fun Char.repeat(count: Int): String = this.toString().repeat(count)
