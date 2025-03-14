package org.jetbrains.dokka.base.transformers.documentables

import org.jetbrains.dokka.Platform
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.plugability.DokkaContext

class KotlinArrayDocumentableReplacerTransformer(context: DokkaContext):
    DocumentableReplacerTransformer(context) {

    private fun Documentable.isJVM() =
        sourceSets.any{ it.analysisPlatform == Platform.jvm }

    override fun processGenericTypeConstructor(genericTypeConstructor: GenericTypeConstructor): AnyWithChanges<GenericTypeConstructor> =
        genericTypeConstructor.takeIf { genericTypeConstructor.dri == DRI("kotlin", "Array") }
            ?.let {
                with(it.projections.firstOrNull() as? Variance<Bound>) {
                    with(this?.inner as? GenericTypeConstructor) {
                        when (this?.dri) {
                            DRI("kotlin", "Int") ->
                                AnyWithChanges(
                                    GenericTypeConstructor(DRI("kotlin", "IntArray"), emptyList()),
                                    true)
                            DRI("kotlin", "Boolean") ->
                                AnyWithChanges(
                                    GenericTypeConstructor(DRI("kotlin", "BooleanArray"), emptyList()),
                                    true)
                            DRI("kotlin", "Float") ->
                                AnyWithChanges(
                                    GenericTypeConstructor(DRI("kotlin", "FloatArray"), emptyList()),
                                    true)
                            DRI("kotlin", "Double") ->
                                AnyWithChanges(
                                    GenericTypeConstructor(DRI("kotlin", "DoubleArray"), emptyList()),
                                    true)
                            DRI("kotlin", "Long") ->
                                AnyWithChanges(
                                    GenericTypeConstructor(DRI("kotlin", "LongArray"), emptyList()),
                                    true)
                            DRI("kotlin", "Short") ->
                                AnyWithChanges(
                                    GenericTypeConstructor(DRI("kotlin", "ShortArray"), emptyList()),
                                    true)
                            DRI("kotlin", "Char") ->
                                AnyWithChanges(
                                    GenericTypeConstructor(DRI("kotlin", "CharArray"), emptyList()),
                                    true)
                            DRI("kotlin", "Byte") ->
                                AnyWithChanges(
                                    GenericTypeConstructor(DRI("kotlin", "ByteArray"), emptyList()),
                                    true)
                            else -> null
                        }
                    }
                }
            }
            ?: super.processGenericTypeConstructor(genericTypeConstructor)

    override fun processModule(module: DModule): AnyWithChanges<DModule>  =
        if(module.isJVM())
            super.processModule(module)
        else AnyWithChanges(module)
}