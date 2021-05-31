import org.jraf.klibnotion.model.block.*
import org.jraf.klibnotion.model.property.SelectOption
import org.jraf.klibnotion.model.richtext.RichTextList

fun <T> T.toFormattedString(): String {
	return when (this) {
		is RichTextList -> plainText?.removeSuffix("\n") ?: ""
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
			is BulletedListItemBlock -> (if(numberedListIndex == 1) "<ol>" else "") + "<li>"
			is Heading1Block -> "<h1>"
			is Heading2Block -> "<h2>"
			is Heading3Block -> "<h3>"
			is NumberedListItemBlock -> (if(numberedListIndex == 1) "<ol>" else "") + "<li>"
			is ParagraphBlock -> "<p>"
			else -> continue
		} + block.text.toFormattedString() + when (block) {
			is BulletedListItemBlock -> "</li>"
			is Heading1Block -> "</h1>"
			is Heading2Block -> "</h2>"
			is Heading3Block -> "</h3>"
			is NumberedListItemBlock -> "</li>"
			is ParagraphBlock -> "</p><br />"
			else -> continue
		})

		// Recurse
		if (!block.children.isNullOrEmpty()) {
			res.append(block.children!!.toFormattedString(level + 1))

			if(block is NumberedListItemBlock) res.append("</ol>")
			else if(block is BulletedListItemBlock) res.append("</ul>")
		}

		if (block is NumberedListItemBlock || block is BulletedListItemBlock) numberedListIndex++ else {
			numberedListIndex = 1
		}
	}
	return res.toString()
}
