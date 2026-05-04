package com.futureschole.liveclass.common.repository

import com.futureschole.liveclass.common.entity.Creator
import org.springframework.data.jpa.repository.JpaRepository

interface CreatorRepository: JpaRepository<Creator, Long>
