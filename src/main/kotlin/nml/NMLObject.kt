package com.ndev.nml

class NMLObject<T : Any>(
    val nodes: List<T>
) {
    override fun toString(): String {
        return nodes.joinToString("\n")
    }
}