package com.example.electionapp.ui.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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

    // ✅ State for Search History
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

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

    // ✅ Add item to history (call this when search is executed)
    fun addToHistory(query: String) {
        if (query.isBlank()) return
        _searchHistory.update { currentList ->
            // Put newest at top, keep unique, limit to 5 items
            (listOf(query.trim()) + currentList).distinct().take(5)
        }
    }

    // ✅ Remove specific item from history
    fun removeFromHistory(query: String) {
        _searchHistory.update { current ->
            current.filterNot { it == query }
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
    "Bangladesh Administration" -> Icons.Default.AccountBalance
    "Bangladesh Army" -> Icons.Default.Security
    "Bangladesh Police" -> Icons.Default.LocalPolice
    "Border Guard (BGB)" -> Icons.Default.Shield
    else -> Icons.Default.Business
}

fun departmentColor(dept: String): Color = when (dept) {
    "Bangladesh Administration" -> Color(0xFF3F51B5) // Royal Indigo for Executive Authority
    "Bangladesh Army" -> Color(0xFF2E7D32)
    "Bangladesh Police" -> Color(0xFF1565C0)
    "Border Guard (BGB)" -> Color(0xFFC62828)
    else -> Color.Gray
}

private fun initialContacts(): List<OfficialContactEntity> = listOf(
    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Army",
        name = "Camp Commander",
        designation = "Major",
        phoneNumber = "01769332446"
    ),
    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Army",
        name = "Captain Tahmid",
        designation = "2IC, Army Camp",
        phoneNumber = "01789317327"
    ),
    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Army",
        name = "ওয়ারেন্ট অফিসার ইউসুফ",
        designation = "ওয়ারেন্ট অফিসার",
        phoneNumber = "01717473855"
    ),

    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Army",
        name = "লেফটেন্যান্ট ফাহিম শাহরিয়ার",
        designation = "লেফটেন্যান্ট",
        phoneNumber = "01304757989"
    ),

    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Army",
        name = "ক্যাপ্টেন ইশতিয়াক হোসেন নাসিফ",
        designation = "ক্যাপ্টেন",
        phoneNumber = "01789317327"
    ),

    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Army",
        name = "লেফটেন্যান্ট পারমিতা",
        designation = "লেফটেন্যান্ট",
        phoneNumber = "01728583309"
    ),

    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Army",
        name = "ক্যাপ্টেন নূর কুতুবুল আলম",
        designation = "ক্যাপ্টেন",
        phoneNumber = "01769212639"
    ),

    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Army",
        name = "ওয়ারেন্ট অফিসার আজহার",
        designation = "ওয়ারেন্ট অফিসার",
        phoneNumber = "01726855645"
    ),
    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Army",
        name = "মেজর শেখ মোঃ ইশতিয়াক উদ্দিন",
        designation = "মেজর",
        phoneNumber = "01763405550"
    ),

    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Police",
        name = "OC Sakhipur",
        designation = "Officer in Charge",
        phoneNumber = "01320096521"
    ),
    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Administration",
        name = "Md. Abdullah Al Rony",
        designation = "UNO",
        phoneNumber = "01762691631"
    ),
    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Administration",
        name = "জনাব শামসুন নাহার শিলা",
        designation = "সহকারী কমিশনার (ভূমি) ও এক্সিকিউটিভ ম্যাজিস্ট্রেট",
        phoneNumber = "0173894559701"
    ),
    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Administration",
        name = "জনাব মোঃ আনোয়ার হোসেন",
        designation = "প্রধান রাজস্ব কর্মকর্তা",
        phoneNumber = "01304074961"
    ),
    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Administration",
        name = "জনাব মিজ্ ইশরাত জাহান",
        designation = "উপসচিব, বিজ্ঞান ও প্রযুক্তি মন্ত্রণালয়",
        phoneNumber = "01723009358"
    ),
    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Administration",
        name = "জনাব টি, এম, এ, মুকিত",
        designation = "সহকারী কমিশনার ও এক্সিকিউটিভ ম্যাজিস্ট্রেট",
        phoneNumber = "01761024660"
    ),
    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Administration",
        name = "জনাব মোহাম্মদ আল আমিন সরকার",
        designation = "নির্বাহী কর্মকর্তা, জেলা পরিষদ",
        phoneNumber = "01736555536"
    ),

    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Administration",
        name = "জনাব মো: সাইফুল ইসলাম",
        designation = "সিনিয়র সহকারী কমিশনার ও এক্সিকিউটিভ ম্যাজিস্ট্রেট",
        phoneNumber = "01749392851"
    ),

    OfficialContactEntity(
        id = 0,
        department = "Bangladesh Administration",
        name = "জনাব জাকির হোসেন",
        designation = "উপসচিব, অর্থবিভাগ",
        phoneNumber = "01763191898"
    ),

    OfficialContactEntity(
        id = 0,
        department = "Border Guard (BGB)",
        name = "নায়েব সুবেদার মোঃ ফরিদ হোসেন",
        designation = "নায়েব সুবেদার",
        phoneNumber = "01769613434"
    ),

    OfficialContactEntity(
        id = 0,
        department = "Border Guard (BGB)",
        name = "হাবিলদার মোঃ শহিদুল ইসলাম",
        designation = "হাবিলদার",
        phoneNumber = "01739341879"
    )

)


