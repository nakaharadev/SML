package com.ndev.nml

class SMLObject(
    val nodes: List<Any>
) {
    override fun toString(): String {
        return nodes.joinToString("\n")
    }
}