package org.jetbrains.dokka.pages

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DisplaySourceSet
import org.jetbrains.dokka.model.WithChildren
import org.jetbrains.dokka.model.properties.PropertyContainer
import org.jetbrains.dokka.model.properties.WithExtraProperties

data class DCI(val dri: Set<DRI>, val kind: Kind) {
    override fun toString() = "$dri[$kind]"
}

interface ContentNode : WithExtraProperties<ContentNode>, WithChildren<ContentNode> {
    val dci: DCI
    val sourceSets: Set<DisplaySourceSet>
    val style: Set<Style>

    fun hasAnyContent(): Boolean

    fun withSourceSets(sourceSets: Set<DisplaySourceSet>): ContentNode

    override val children: List<ContentNode>
        get() = emptyList()
}

/** Simple text */
data class ContentText(
    val text: String,
    override val dci: DCI,
    override val sourceSets: Set<DisplaySourceSet>,
    override val style: Set<Style> = emptySet(),
    override val extra: PropertyContainer<ContentNode> = PropertyContainer.empty()
) : ContentNode {
    override fun withNewExtras(newExtras: PropertyContainer<ContentNode>): ContentText = copy(extra = newExtras)
    override fun withSourceSets(sourceSets: Set<DisplaySourceSet>): ContentText = copy(sourceSets = sourceSets)
    override fun hasAnyContent(): Boolean = text.isNotBlank()
}

data class ContentBreakLine(
    override val sourceSets: Set<DisplaySourceSet>,
    override val dci: DCI = DCI(emptySet(), ContentKind.Empty),
    override val style: Set<Style> = emptySet(),
    override val extra: PropertyContainer<ContentNode> = PropertyContainer.empty()
) : ContentNode {
    override fun withNewExtras(newExtras: PropertyContainer<ContentNode>): ContentBreakLine = copy(extra = newExtras)
    override fun withSourceSets(sourceSets: Set<DisplaySourceSet>): ContentBreakLine = copy(sourceSets = sourceSets)
    override fun hasAnyContent(): Boolean = true
}

/** Headers */
data class ContentHeader(
    override val children: List<ContentNode>,
    val level: Int,
    override val dci: DCI,
    override val sourceSets: Set<DisplaySourceSet>,
    override val style: Set<Style>,
    override val extra: PropertyContainer<ContentNode> = PropertyContainer.empty()
) : ContentComposite {
    constructor(level: Int, c: ContentComposite) : this(c.children, level, c.dci, c.sourceSets, c.style, c.extra)

    override fun withNewExtras(newExtras: PropertyContainer<ContentNode>): ContentHeader = copy(extra = newExtras)

    override fun transformChildren(transformer: (ContentNode) -> ContentNode): ContentHeader =
        copy(children = children.map(transformer))

    override fun withSourceSets(sourceSets: Set<DisplaySourceSet>): ContentHeader =
        copy(sourceSets = sourceSets)
}

interface ContentCode : ContentComposite

/** Code blocks */
data class ContentCodeBlock(
    override val children: List<ContentNode>,
    val language: String,
    override val dci: DCI,
    override val sourceSets: Set<DisplaySourceSet>,
    override val style: Set<Style>,
    override val extra: PropertyContainer<ContentNode> = PropertyContainer.empty()
) : ContentCode {
    override fun withNewExtras(newExtras: PropertyContainer<ContentNode>): ContentCodeBlock = copy(extra = newExtras)

    override fun transformChildren(transformer: (ContentNode) -> ContentNode): ContentCodeBlock =
        copy(children = children.map(transformer))

    override fun withSourceSets(sourceSets: Set<DisplaySourceSet>): ContentCodeBlock =
        copy(sourceSets = sourceSets)

}

data class ContentCodeInline(
    override val children: List<ContentNode>,
    val language: String,
    override val dci: DCI,
    override val sourceSets: Set<DisplaySourceSet>,
    override val style: Set<Style>,
    override val extra: PropertyContainer<ContentNode> = PropertyContainer.empty()
) : ContentCode {
    override fun withNewExtras(newExtras: PropertyContainer<ContentNode>): ContentCodeInline = copy(extra = newExtras)

    override fun transformChildren(transformer: (ContentNode) -> ContentNode): ContentCodeInline =
        copy(children = children.map(transformer))

    override fun withSourceSets(sourceSets: Set<DisplaySourceSet>): ContentCodeInline =
        copy(sourceSets = sourceSets)

}

/** Union type replacement */
interface ContentLink : ContentComposite

/** All links to classes, packages, etc. that have te be resolved */
data class ContentDRILink(
    override val children: List<ContentNode>,
    val address: DRI,
    override val dci: DCI,
    override val sourceSets: Set<DisplaySourceSet>,
    override val style: Set<Style> = emptySet(),
    override val extra: PropertyContainer<ContentNode> = PropertyContainer.empty()
) : ContentLink {
    override fun withNewExtras(newExtras: PropertyContainer<ContentNode>): ContentDRILink = copy(extra = newExtras)

    override fun transformChildren(transformer: (ContentNode) -> ContentNode): ContentDRILink =
        copy(children = children.map(transformer))

    override fun withSourceSets(sourceSets: Set<DisplaySourceSet>): ContentDRILink =
        copy(sourceSets = sourceSets)

}

/** All links that do not need to be resolved */
data class ContentResolvedLink(
    override val children: List<ContentNode>,
    val address: String,
    override val dci: DCI,
    override val sourceSets: Set<DisplaySourceSet>,
    override val style: Set<Style> = emptySet(),
    override val extra: PropertyContainer<ContentNode> = PropertyContainer.empty()
) : ContentLink {
    override fun withNewExtras(newExtras: PropertyContainer<ContentNode>): ContentResolvedLink =
        copy(extra = newExtras)

    override fun transformChildren(transformer: (ContentNode) -> ContentNode): ContentResolvedLink =
        copy(children = children.map(transformer))

    override fun withSourceSets(sourceSets: Set<DisplaySourceSet>): ContentResolvedLink =
        copy(sourceSets = sourceSets)
}

/** Embedded resources like images */
data class ContentEmbeddedResource(
    override val children: List<ContentNode> = emptyList(),
    val address: String,
    val altText: String?,
    override val dci: DCI,
    override val sourceSets: Set<DisplaySourceSet>,
    override val style: Set<Style> = emptySet(),
    override val extra: PropertyContainer<ContentNode> = PropertyContainer.empty()
) : ContentLink {
    override fun withNewExtras(newExtras: PropertyContainer<ContentNode>): ContentEmbeddedResource =
        copy(extra = newExtras)

    override fun transformChildren(transformer: (ContentNode) -> ContentNode): ContentEmbeddedResource =
        copy(children = children.map(transformer))

    override fun withSourceSets(sourceSets: Set<DisplaySourceSet>): ContentEmbeddedResource =
        copy(sourceSets = sourceSets)
}

/** Logical grouping of [ContentNode]s  */
interface ContentComposite : ContentNode {
    override val children: List<ContentNode> // overwrite to make it abstract once again

    override val sourceSets: Set<DisplaySourceSet> get() = children.flatMap { it.sourceSets }.toSet()

    fun transformChildren(transformer: (ContentNode) -> ContentNode): ContentComposite

    override fun hasAnyContent(): Boolean = children.any { it.hasAnyContent() }
}

/** Tables */
data class ContentTable(
    val header: List<ContentGroup>,
    val caption: ContentGroup? = null,
    override val children: List<ContentGroup>,
    override val dci: DCI,
    override val sourceSets: Set<DisplaySourceSet>,
    override val style: Set<Style>,
    override val extra: PropertyContainer<ContentNode> = PropertyContainer.empty()
) : ContentComposite {
    override fun withNewExtras(newExtras: PropertyContainer<ContentNode>): ContentTable = copy(extra = newExtras)

    override fun transformChildren(transformer: (ContentNode) -> ContentNode): ContentTable =
        copy(children = children.map(transformer).map { it as ContentGroup })

    override fun withSourceSets(sourceSets: Set<DisplaySourceSet>): ContentTable =
        copy(sourceSets = sourceSets)

}

/** Lists */
data class ContentList(
    override val children: List<ContentNode>,
    val ordered: Boolean,
    override val dci: DCI,
    override val sourceSets: Set<DisplaySourceSet>,
    override val style: Set<Style>,
    override val extra: PropertyContainer<ContentNode> = PropertyContainer.empty()
) : ContentComposite {
    override fun withNewExtras(newExtras: PropertyContainer<ContentNode>): ContentList = copy(extra = newExtras)

    override fun transformChildren(transformer: (ContentNode) -> ContentNode): ContentList =
        copy(children = children.map(transformer))

    override fun withSourceSets(sourceSets: Set<DisplaySourceSet>): ContentList =
        copy(sourceSets = sourceSets)
}

/** Default group, eg. for blocks of Functions, Properties, etc. **/
data class ContentGroup(
    override val children: List<ContentNode>,
    override val dci: DCI,
    override val sourceSets: Set<DisplaySourceSet>,
    override val style: Set<Style>,
    override val extra: PropertyContainer<ContentNode> = PropertyContainer.empty()
) : ContentComposite {
    override fun withNewExtras(newExtras: PropertyContainer<ContentNode>): ContentGroup = copy(extra = newExtras)

    override fun transformChildren(transformer: (ContentNode) -> ContentNode): ContentGroup =
        copy(children = children.map(transformer))

    override fun withSourceSets(sourceSets: Set<DisplaySourceSet>): ContentGroup =
        copy(sourceSets = sourceSets)
}

/**
 * @property groupID is used for finding and copying [ContentDivergentInstance]s when merging [ContentPage]s
 */
data class ContentDivergentGroup(
    override val children: List<ContentDivergentInstance>,
    override val dci: DCI,
    override val style: Set<Style>,
    override val extra: PropertyContainer<ContentNode>,
    val groupID: GroupID,
    val implicitlySourceSetHinted: Boolean = true
) : ContentComposite {
    data class GroupID(val name: String)

    override val sourceSets: Set<DisplaySourceSet>
        get() = children.flatMap { it.sourceSets }.distinct().toSet()

    override fun withNewExtras(newExtras: PropertyContainer<ContentNode>): ContentDivergentGroup =
        copy(extra = newExtras)

    override fun transformChildren(transformer: (ContentNode) -> ContentNode): ContentDivergentGroup =
        copy(children = children.map(transformer).map { it as ContentDivergentInstance })

    override fun withSourceSets(sourceSets: Set<DisplaySourceSet>): ContentDivergentGroup = this
}

/** Instance of a divergent content */
data class ContentDivergentInstance(
    val before: ContentNode?,
    val divergent: ContentNode,
    val after: ContentNode?,
    override val dci: DCI,
    override val sourceSets: Set<DisplaySourceSet>,
    override val style: Set<Style>,
    override val extra: PropertyContainer<ContentNode> = PropertyContainer.empty()
) : ContentComposite {
    override val children: List<ContentNode>
        get() = listOfNotNull(before, divergent, after)

    override fun withNewExtras(newExtras: PropertyContainer<ContentNode>): ContentDivergentInstance =
        copy(extra = newExtras)

    override fun transformChildren(transformer: (ContentNode) -> ContentNode): ContentDivergentInstance =
        copy(
            before = before?.let(transformer),
            divergent = divergent.let(transformer),
            after = after?.let(transformer)
        )

    override fun withSourceSets(sourceSets: Set<DisplaySourceSet>): ContentDivergentInstance =
        copy(sourceSets = sourceSets)

}

data class PlatformHintedContent(
    val inner: ContentNode,
    override val sourceSets: Set<DisplaySourceSet>
) : ContentComposite {
    override val children = listOf(inner)

    override val dci: DCI
        get() = inner.dci

    override val extra: PropertyContainer<ContentNode>
        get() = inner.extra

    override val style: Set<Style>
        get() = inner.style

    override fun withNewExtras(newExtras: PropertyContainer<ContentNode>) =
        throw UnsupportedOperationException("This method should not be called on this PlatformHintedContent")

    override fun transformChildren(transformer: (ContentNode) -> ContentNode): PlatformHintedContent =
        copy(inner = transformer(inner))

    override fun withSourceSets(sourceSets: Set<DisplaySourceSet>): PlatformHintedContent =
        copy(sourceSets = sourceSets)

}

interface Style
interface Kind

/**
 * [ContentKind] represents a grouping of content of one kind that can can be rendered
 * as part of a composite page (one tab/block within a class's page, for instance).
 */
enum class ContentKind : Kind {

    /**
     * Marks all sorts of signatures. Can contain sub-kinds marked as [SymbolContentKind]
     *
     * Some examples:
     * - primary constructor: `data class CoroutineName(name: String) : AbstractCoroutineContextElement`
     * - constructor: `fun CoroutineName(name: String)`
     * - function: `open override fun toString(): String`
     * - property: `val name: String`
     */
    Symbol,

    Comment, Constructors, Functions, Parameters, Properties, Classlikes, Packages, Sample, Main, BriefComment,
    Empty, Source, TypeAliases, Cover, Inheritors, SourceSetDependentHint, Extensions, Annotations,

    /**
     * Deprecation details block with related information such as message/replaceWith/level.
     */
    Deprecation;

    companion object {
        private val platformTagged =
            setOf(
                Constructors,
                Functions,
                Properties,
                Classlikes,
                Packages,
                Source,
                TypeAliases,
                Inheritors,
                Extensions
            )

        fun shouldBePlatformTagged(kind: Kind): Boolean = kind in platformTagged
    }
}

/**
 * Content kind for [ContentKind.Symbol] content, which is essentially about signatures
 */
enum class SymbolContentKind : Kind {
    /**
     * Marks constructor/function parameters, everything in-between parentheses.
     *
     * For function `fun foo(bar: String, baz: Int, qux: Boolean)`,
     * the parameters would be the whole of `bar: String, baz: Int, qux: Boolean`
     */
    Parameters,

    /**
     * Marks a single parameter in a function. Most likely to be a child of [Parameters].
     *
     * In function `fun foo(bar: String, baz: Int, qux: Boolean)` there would be 3 [Parameter] instances:
     * - `bar: String, `
     * - `baz: Int, `
     * - `qux: Boolean`
     */
    Parameter,
}

enum class TokenStyle : Style {
    Keyword, Punctuation, Function, Operator, Annotation, Number, String, Boolean, Constant
}

enum class TextStyle : Style {
    Bold, Italic, Strong, Strikethrough, Paragraph,
    Block, Span, Monospace, Indented, Cover, UnderCoverText, BreakableAfter, Breakable, InlineComment, Quotation,
    FloatingRight, Var, Underlined
}

enum class ContentStyle : Style {
    RowTitle,
    /**
     * The style is used only for HTML. It is applied only for [ContentGroup].
     * Creating and rendering tabs is a part of a renderer.
     */
    TabbedContent,

    WithExtraAttributes, RunnableSample, InDocumentationAnchor, Caption,
    Wrapped, Indented, KDocTag, Footnote
}

enum class ListStyle : Style {
    /**
     * Represents a list of groups of [DescriptionTerm] and [DescriptionDetails].
     * Common uses for this element are to implement a glossary or to display
     * metadata (a list of key-value pairs). Formatting example: see `<dl>` html tag.
     */
    DescriptionList,

    /**
     * If used within [DescriptionList] context, specifies a term in a description
     * or definition list, usually followed by [DescriptionDetails] for one or more
     * terms. Formatting example: see `<dt>` html tag
     */
    DescriptionTerm,

    /**
     * If used within [DescriptionList] context, provides the definition or other
     * related text associated with [DescriptionTerm]. Formatting example: see `<dd>` html tag
     */
    DescriptionDetails
}

object CommentTable : Style

object MultimoduleTable : Style

fun ContentNode.hasStyle(style: Style) = this.style.contains(style)
