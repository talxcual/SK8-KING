package com.Ktoledo.pissdrunxking

class PDKKingsManager {
    companion object {
        fun getMockKingsSemanales(): List<PDKKing> {
            return listOf(
                PDKKing("1", "TonyHawk99", 1, 1420, "mock_profile_1"),
                PDKKing("2", "NyjahH", 2, 1315, "mock_profile_2"),
                PDKKing("3", "LeticiaB", 3, 980, "mock_profile_3"),
                PDKKing("4", "P-Rod", 4, 850, "mock_profile_4"),
                PDKKing("5", "YutoHorigome", 5, 710, "mock_profile_5")
            )
        }
    }
}
