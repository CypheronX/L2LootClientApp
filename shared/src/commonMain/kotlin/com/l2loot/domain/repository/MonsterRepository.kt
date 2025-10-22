package com.l2loot.domain.repository

import com.l2loot.domain.model.MonsterQueryParams
import com.l2loot.domain.model.MonsterResult
import kotlinx.coroutines.flow.Flow

interface MonsterRepository {
    fun getMonsters(params: MonsterQueryParams): Flow<Result<List<MonsterResult>>>
}