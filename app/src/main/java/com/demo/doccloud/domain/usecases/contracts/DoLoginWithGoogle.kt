package com.demo.doccloud.domain.usecases.contracts

import android.content.Intent
import com.demo.doccloud.domain.entities.User

interface DoLoginWithGoogle {
    suspend operator fun invoke(intent: Intent?) : User
}