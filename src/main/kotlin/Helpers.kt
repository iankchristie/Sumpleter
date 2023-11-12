inline fun <T, reified V> List<List<T>>.matrixMap(function: (T) -> V): List<List<V>> {
    return this.map { row ->
        row.map { entry ->
            function.invoke(entry)
        }
    }
}

inline fun <T> List<List<T>>.matrixAll(function: (T) -> Boolean): Boolean {
    this.forEach { row ->
        row.forEach { entry ->
            if (!function.invoke(entry)) {
                return false
            }
        }
    }
    return true
}

inline fun <T, reified V> List<List<T>>.matrixMapRows(function: (List<T>) -> V): List<V> {
    return this.map { row ->
        function.invoke(row)
    }
}

inline fun <reified T, reified V> List<List<T>>.matrixMapColumns(function: (List<T>) -> V): List<V> {
    val size = this.size
    return (0 until size).map { colNum ->
        val colSlice = Array(size) { rowNum -> this[rowNum][colNum] }.asList()
        return@map function.invoke(colSlice)
    }
}


inline fun <T, reified V> List<List<T>>.matrixMapIndexed(function: (row: Int, col: Int, T) -> V): List<List<V>> {
    return this.mapIndexed { rowNum, row ->
        row.mapIndexed { colNum, entry ->
            function.invoke(rowNum, colNum, entry)
        }
    }
}

inline fun <T, reified V> List<List<T>>.matrixFoldIndexed(initialValue: V, function: (row: Int, col: Int, acc: V, T) -> V): V {
    return this.foldIndexed(initialValue) { rowNum, rowAcc, row ->
        row.foldIndexed(rowAcc) { colNum, acc, entry ->
            function.invoke(rowNum, colNum, acc, entry)
        }
    }
}

inline fun <reified T> Array<Array<T>>.row(index: Int): Array<T> {
    return this[index]
}

inline fun <reified T> Array<Array<T>>.column(index: Int): Array<T> {
    return Array(this.first().size) { rowNum -> this[rowNum][index] }
}

fun <T> List<List<T>>.toMutableCopy(): MutableList<MutableList<T>> {
    return this.map { it.toMutableList() }.toMutableList()
}

inline fun <reified T> List<List<T>>.row(index: Int): List<T> {
    return this[index]
}

inline fun <reified T> List<List<T>>.column(index: Int): List<T> {
    return List(this.first().size) { rowNum -> this[rowNum][index] }
}

inline fun <reified T> List<List<T>>.subMatrix(rows: IntRange, columns: IntRange): List<List<T>> {
    val subMatrix = mutableListOf<MutableList<T>>()
    rows.forEach { row ->
        columns.forEach { column ->
            subMatrix[row][column] = this[row][column]
        }
    }
    return subMatrix
}