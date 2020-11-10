package com.example.playground.paging.model

/**
 * To add separator items in our recyclerview
 * we need an object that can encapsulate both
 * item types that the list can show. Thus I
 * give you the sealed class UiModel!
 */
sealed class UiModel {
    data class RepoItem(val repo: RepoModel) : UiModel()
    data class SeparatorItem(val description: String) : UiModel()
}