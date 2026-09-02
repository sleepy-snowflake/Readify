package com.sleepy.readify.core.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArticleDocumentSerializationTest {

    private val json = Json

    @Test
    fun fullDocumentRoundTrips() {
        val document = ArticleDocument(
            title = "Example Article",
            author = "A. Author",
            date = "2026-08-29T10:00:00Z",
            source = "example.com",
            cover = "/images/cover.jpg",
            blocks = listOf(
                Block.Heading(
                    level = 2,
                    runs = listOf(InlineRun(InlineType.TEXT, "Section")),
                ),
                Block.Paragraph(
                    runs = listOf(
                        InlineRun(InlineType.TEXT, "plain "),
                        InlineRun(InlineType.EM, "emphasized"),
                        InlineRun(InlineType.STRONG, "strong"),
                        InlineRun(InlineType.CODE, "x = 1"),
                        InlineRun(InlineType.LINK, "linked", href = "https://example.com"),
                    ),
                ),
                Block.Image(src = "img/1.jpg", alt = "one"),
                Block.Figure(src = "img/2.jpg", caption = "two"),
                Block.Quote(
                    runs = listOf(InlineRun(InlineType.TEXT, "quoted")),
                    cite = "someone",
                ),
                Block.Code(text = "val x = 1", lang = "kotlin"),
                Block.ListBlock(
                    ordered = true,
                    items = listOf(
                        ListItem(runs = listOf(InlineRun(InlineType.TEXT, "one"))),
                        ListItem(
                            list = Block.ListBlock(
                                items = listOf(
                                    ListItem(runs = listOf(InlineRun(InlineType.TEXT, "nested"))),
                                ),
                            ),
                        ),
                    ),
                ),
                Block.Table(
                    rows = listOf(
                        listOf(TableCell(runs = listOf(InlineRun(InlineType.TEXT, "header")))),
                        listOf(TableCell(runs = listOf(InlineRun(InlineType.TEXT, "cell")))),
                    ),
                    header = true,
                ),
                Block.Hr,
            ),
        )

        val encoded = json.encodeToString(document)
        assertEquals(document, json.decodeFromString<ArticleDocument>(encoded))
    }

    @Test
    fun inlineRunUsesContractKeys() {
        val encoded = json.encodeToString(InlineRun(InlineType.LINK, "a", href = "https://e.co"))
        assertTrue("\"t\":\"link\"" in encoded)
        assertTrue("\"s\":\"a\"" in encoded)
        assertTrue("\"href\":\"https://e.co\"" in encoded)
    }

    @Test
    fun decodesArchitectureSample() {
        val sample = """
            {"title":"T","blocks":[
              {"type":"heading","level":2,"runs":[{"t":"text","s":"H"}]},
              {"type":"paragraph","runs":[{"t":"text","s":"p"}]},
              {"type":"image","src":"a.jpg"},
              {"type":"figure","src":"b.jpg","caption":"c"},
              {"type":"quote","runs":[{"t":"em","s":"q"}],"cite":"x"},
              {"type":"code","text":"x","lang":"js"},
              {"type":"list","ordered":false,"items":[
                {"runs":[{"t":"strong","s":"i"}]},
                {"list":{"ordered":true,"items":[{"runs":[{"t":"text","s":"n"}]}]}}
              ]},
              {"type":"table","rows":[[{"runs":[{"t":"text","s":"c"}]}]],"header":true},
              {"type":"hr"}
            ]}
        """.trimIndent()

        val document = json.decodeFromString<ArticleDocument>(sample)

        assertEquals(9, document.blocks.size)
        assertEquals(document, json.decodeFromString(json.encodeToString(document)))
    }
}
