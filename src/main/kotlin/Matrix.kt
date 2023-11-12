data class Matrix<T>(val entries: List<List<T>>) {
    fun <V> map(function: (T) -> V): Matrix<V> {
        return Matrix(
            entries.map { row ->
                row.map { entry ->
                    function.invoke(entry)
                }
            }
        )
    }

    fun row(index: Int): List<T> {
        return entries[index]
    }

    fun columns(index: Int): List<T> {
        return List(entries.first().size) { rowNum -> entries[rowNum][index] }
    }

    fun subMatrix(rows: IntRange, columns: IntRange): Matrix<T> {
        val subMatrix = mutableListOf<MutableList<T>>()
        rows.forEach { row ->
            columns.forEach { column ->
                subMatrix[row][column] = entries[row][column]
            }
        }
        return Matrix(subMatrix)
    }
}
