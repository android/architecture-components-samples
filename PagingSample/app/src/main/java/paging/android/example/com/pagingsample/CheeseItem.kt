package paging.android.example.com.pagingsample

/**
 * Common UI model between the [Cheese] data class and separators.
 */
sealed class CheeseItem(val name: String) {
    data class Item(val cheese: Cheese) : CheeseItem(cheese.name)
    data class Separator(private val letter: Char) : CheeseItem(letter.toUpperCase().toString())
}