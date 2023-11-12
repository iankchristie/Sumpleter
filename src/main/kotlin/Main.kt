fun main() {
    val boardSize = 9
    val board = SumpleteBoard.generateFromBoard()
    SumpleteResolver(board).run()
}
