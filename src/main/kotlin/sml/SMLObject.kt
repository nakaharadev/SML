package com.ndev.sml

class SMLObject(
    val nodes: List<Any>
) {
    override fun toString(): String {
        return nodes.joinToString("\n")
    }
}