package com.katoki.voicenavigation.parser

/**
 * CommandParser interprets spoken text and converts it into actionable commands.
 */
class CommandParser {
    
    /**
     * Represents a parsed command with its type and parameters.
     */
    sealed class Command {
        data class Tap(val position: String) : Command()
        data class Swipe(val startPosition: String, val endPosition: String) : Command()
        data class Circle(val centerPosition: String) : Command()
        object Home : Command()
        object Back : Command()
        object Recents : Command()
        object Unknown : Command()
    }
    
    /**
     * Parses spoken text into a Command.
     * @param spokenText The text from speech recognition
     * @return A Command object representing the parsed command
     */
    fun parse(spokenText: String): Command {
        val text = spokenText.trim().lowercase()
        val swipeUp = Command.Swipe("E8", "E4") // 向上划/上滑
        val swipeDown = Command.Swipe("E4", "E8")
        val swipeLeft = Command.Swipe("F5", "D5")
        val swipeRight = Command.Swipe("D5", "F5")

        return when {
            // Home command
            text.contains("home") -> Command.Home
            text.contains("主页") -> Command.Home

            // Back command
            text.contains("back") -> Command.Back
            text.contains("返回") -> Command.Back
            text.contains("退出") -> Command.Back

            // Recents/Recent apps command
            text.contains("recent") -> Command.Recents
            text.contains("应用历史") -> Command.Recents
            text.contains("历史应用") -> Command.Recents

            // Tap command: "tap A5" or "click B3"
            text.startsWith("tap ") || text.startsWith("click ") -> {
                val position = extractPosition(text)
                if (position != null) {
                    Command.Tap(position)
                } else {
                    Command.Unknown
                }
            }

            // Swipe command: "swipe A1 to C5" or "swipe from A1 to C5"
            text.startsWith("swipe") -> {
                parseSwipeCommand(text)
            }


            text.contains("上划") -> swipeUp
            text.contains("上滑") -> swipeUp
            text.contains("向上") -> swipeUp
            text.contains("下滑") -> swipeDown
            text.contains("下划") -> swipeDown
            text.contains("向下") -> swipeDown
            text.contains("左划") -> swipeLeft
            text.contains("左滑") -> swipeLeft
            text.contains("向左") -> swipeLeft
            text.contains("右划") -> swipeRight
            text.contains("右滑") -> swipeRight
            text.contains("向右") -> swipeRight

            text.contains("下一个") -> parsePageCommand("pagedown")
            text.contains("下一页") -> parsePageCommand("pagedown")
            text.contains("上一个") -> parsePageCommand("pageup")
            text.contains("上一页") -> parsePageCommand("pageup")

            // Page command: "pagedown/pageup" or "page down/up"
            text.startsWith("page") -> {
                parsePageCommand(text)
            }

            // Circle command: "circle B3" or "draw circle at B3"
            text.contains("circle") -> {
                val position = extractPosition(text)
                if (position != null) {
                    Command.Circle(position)
                } else {
                    Command.Unknown
                }
            }

            else -> Command.Unknown
        }
    }
    
    /**
     * Parses a swipe command to extract start and end positions.
     */
    private fun parseSwipeCommand(text: String): Command {
        // Pattern: "swipe A1 to C5" or "swipe from A1 to C5"
        val parts = text.split(" ")
        
        var startPos: String? = null
        var endPos: String? = null
        
        for (i in parts.indices) {
            val part = parts[i]
            if (isGridPosition(part)) {
                if (startPos == null) {
                    startPos = part.uppercase()
                } else if (endPos == null) {
                    endPos = part.uppercase()
                    break
                }
            }
        }
        
        return if (startPos != null && endPos != null) {
            Command.Swipe(startPos, endPos)
        } else {
            Command.Unknown
        }
    }

    /**
     * Parses a pagedown/pageup command
     */
    private fun parsePageCommand(text: String): Command {
        val parts = text.replace(" ", "")

        return if (parts.contains("pagedown") || parts.contains("pagedone")) {
            Command.Swipe("C8", "C4")
        } else if (parts.contains("pageup")) {
            Command.Swipe("C4", "C8")
        } else {
            Command.Unknown
        }
    }
    
    /**
     * Extracts a grid position from text.
     */
    private fun extractPosition(text: String): String? {
        val parts = text.split(" ")
        for (part in parts) {
            if (isGridPosition(part)) {
                return part.uppercase()
            }
        }
        return null
    }
    
    /**
     * Checks if a string matches the grid position pattern.
     */
    private fun isGridPosition(text: String): Boolean {
        if (text.length < 2 || text.length > 3) return false
        
        val firstChar = text[0].uppercaseChar()
        if (firstChar < 'A' || firstChar > 'J') return false
        
        val number = text.substring(1).toIntOrNull() ?: return false
        return number in 1..10
    }
}
