package com.example.electionapp.ui.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electionapp.data.local.dao.OfficialContactDao
import com.example.electionapp.data.local.entity.OfficialContactEntity
import com.example.electionapp.ui.admin.model.ContactPerson
import com.example.electionapp.ui.admin.model.Department
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminContactsViewModel @Inject constructor(
    private val dao: OfficialContactDao
) : ViewModel() {



    // Maps database entities to UI models including the unique ID
    val departments: StateFlow<List<Department>> =
        dao.observeContacts()
            .map { entities ->
                entities.groupBy { it.department }
                    .map { (deptName, contacts) ->
                        Department(
                            title = deptName,
                            icon = departmentIcon(deptName),
                            accentColor = departmentColor(deptName),
                            contacts = contacts.map {
                                // Map all fields including ID and Department
                                ContactPerson(it.id, it.name, it.designation, it.phoneNumber, it.department)
                            }
                        )
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(contact: OfficialContactEntity) {
        viewModelScope.launch { dao.upsert(contact) }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            // Create a dummy entity with the ID to trigger deletion
            dao.softDelete(id.toLong())
        }
    }

    fun save2(entity: OfficialContactEntity) {
        viewModelScope.launch {
            dao.upsert(entity)
        }
    }

    init {
        seedIfEmpty()
    }

    private fun seedIfEmpty() {
        viewModelScope.launch {
            val isEmpty = dao.observeContacts().first().isEmpty()
            if (isEmpty) {
                dao.insertAll(initialContacts())
            }
        }
    }
}



/* ---------- UI PRESERVATION HELPERS ---------- */

fun departmentIcon(dept: String): ImageVector = when (dept) {
    "Bangladesh Army" -> Icons.Default.Security
    "Bangladesh Police" -> Icons.Default.LocalPolice
    "Border Guard (BGB)" -> Icons.Default.Business
    else -> Icons.Default.Business
}

fun departmentColor(dept: String): Color = when (dept) {
    "Bangladesh Army" -> Color(0xFF2E7D32)
    "Bangladesh Police" -> Color(0xFF1565C0)
    "Border Guard (BGB)" -> Color(0xFFC62828)
    else -> Color.Gray
}

private fun initialContacts(): List<OfficialContactEntity> = listOf(
    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Army",
        name = "Col. Kamal Ahmed",
        designation = "Sector Commander",
        phoneNumber = "01711000000"
    ),
    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Army",
        name = "Maj. Rafiqul Islam",
        designation = "Operations Officer",
        phoneNumber = "01711111111"
    ),
    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Police",
        name = "SP Mahmudullah",
        designation = "District Superintendent",
        phoneNumber = "01811000000"
    ),
    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Police",
        name = "ASP Nusrat Jahan",
        designation = "HQ Coordinator",
        phoneNumber = "01811333333"
    ),
    OfficialContactEntity(
        id = 0,
        department = "Border Guard (BGB)",
        name = "Lt. Col. Tariqul",
        designation = "Battalion Commander",
        phoneNumber = "01911000000"
    )
)

