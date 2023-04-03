import org.jraf.klibnotion.model.block.*
import org.jraf.klibnotion.model.property.SelectOption
import org.jraf.klibnotion.model.richtext.EquationRichText
import org.jraf.klibnotion.model.richtext.RichText
import org.jraf.klibnotion.model.richtext.RichTextList

fun <T> T.toFormattedString(): String {
    return when (this) {
        is EquationBlock -> "$$${this.expression}$$"
        is EquationRichText -> "$${this.expression}$"
        is ImageBlock -> "<img src='${this.image.url}' alt='${this.caption}' />"
        is ParagraphBlock -> this.text?.toFormattedString() ?: ""
        is Heading1Block -> this.text?.toFormattedString() ?: ""
        is Heading2Block -> this.text?.toFormattedString() ?: ""
        is Heading3Block -> this.text?.toFormattedString() ?: ""
        is RichText -> plainText.toFormattedString()
        is RichTextList -> richTextList.toFormattedString() ?: ""
        is SelectOption -> name
        else -> toString()
    }
}

fun List<Block>.toFormattedString(level: Int = 0): String {
    val res = StringBuilder()
    val levelStr = "  ".repeat(level)
    var numberedListIndex = 1
    for (block in this) {
        res.append(levelStr + when (block) {
            is BulletedListItemBlock -> (if (numberedListIndex == 1) "<ol>" else "") + "<li>"
            is Heading1Block -> "<h1>"
            is Heading2Block -> "<h2>"
            is ImageBlock -> ""
            is Heading3Block -> "<h3>"
            is NumberedListItemBlock -> (if (numberedListIndex == 1) "<ol>" else "") + "<li>"
            is EquationBlock -> "<span class='math'>"
            is ParagraphBlock -> "<p>"
            else -> continue
        } +
                block.toFormattedString()
                + when (block) {
            is BulletedListItemBlock -> "</li>"
            is Heading1Block -> "</h1>"
            is Heading2Block -> "</h2>"
            is Heading3Block -> "</h3>"
            is ImageBlock -> ""
            is NumberedListItemBlock -> "</li>"
            is EquationBlock -> "</span><br />"
            is ParagraphBlock -> "</p><br />"
            else -> continue
        })

        // Recurse
        if (!block.children.isNullOrEmpty()) {
            res.append(block.children!!.toFormattedString(level + 1))

            if (block is NumberedListItemBlock) res.append("</ol>")
            else if (block is BulletedListItemBlock) res.append("</ul>")
        }

        if (block is NumberedListItemBlock || block is BulletedListItemBlock) numberedListIndex++ else {
            numberedListIndex = 1
        }
    }
    return res.toString()
}

fun MutableList<RichText>.toFormattedString(): String? {
    val builder = StringBuilder()

    if (this.isEmpty()) {
        return null
    }

    for (block in this) {
        builder.append(when(block) {
            is EquationRichText -> "<span class='math'>"
            else -> {
                var openingTag = "<span>"

                if (block.annotations.bold) openingTag += "<b>"
                if (block.annotations.italic) openingTag += "<em>"
                openingTag
            }
        } +
                block.toFormattedString()
                + when (block) {
            is EquationRichText -> "</span>"
            else -> {
                var closingTag = ""

                if (block.annotations.italic) closingTag += "</em>"
                if (block.annotations.bold) closingTag += "</b>"
                "$closingTag</span>"
            }
        })
    }

    return builder.toString()
}
