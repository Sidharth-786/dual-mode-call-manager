package com.example.dualmodecallmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dualmodecallmanager.data.db.ContactEntity
import com.example.dualmodecallmanager.data.model.ContactCategory
import com.example.dualmodecallmanager.ui.components.AddContactDialog
import com.example.dualmodecallmanager.ui.components.EditContactDialog
import com.example.dualmodecallmanager.ui.viewmodel.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    state: MainUiState,
    onCategoryFilterChange: (ContactCategory?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAddContact: (String, String, ContactCategory) -> Unit,
    onUpdateContact: (ContactEntity) -> Unit,
    onDeleteContact: (ContactEntity) -> Unit,
    onRequestImportContacts: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<ContactEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header & Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Permitted Contacts",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Assign contacts to Work, Personal, or Both",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(onClick = onRequestImportContacts) {
                    Icon(imageVector = Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import")
                }
            }

            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search by name or number...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            // Category Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedCategoryFilter == null,
                    onClick = { onCategoryFilterChange(null) },
                    label = { Text("All (${state.contacts.size})") }
                )
                FilterChip(
                    selected = state.selectedCategoryFilter == ContactCategory.WORK,
                    onClick = { onCategoryFilterChange(ContactCategory.WORK) },
                    label = { Text("Work") }
                )
                FilterChip(
                    selected = state.selectedCategoryFilter == ContactCategory.PERSONAL,
                    onClick = { onCategoryFilterChange(ContactCategory.PERSONAL) },
                    label = { Text("Personal") }
                )
                FilterChip(
                    selected = state.selectedCategoryFilter == ContactCategory.BOTH,
                    onClick = { onCategoryFilterChange(ContactCategory.BOTH) },
                    label = { Text("Both") }
                )
            }

            // Contacts List
            if (state.contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No contacts found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Tap + to add or Import from phone contacts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.contacts, key = { it.id }) { contact ->
                        ContactItemCard(
                            contact = contact,
                            onEdit = { editingContact = contact },
                            onDelete = { onDeleteContact(contact) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, number, category ->
                onAddContact(name, number, category)
                showAddDialog = false
            }
        )
    }

    editingContact?.let { contact ->
        EditContactDialog(
            contact = contact,
            onDismiss = { editingContact = null },
            onConfirm = { updated ->
                onUpdateContact(updated)
                editingContact = null
            }
        )
    }
}

@Composable
private fun ContactItemCard(
    contact: ContactEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        when (contact.category) {
                            ContactCategory.WORK -> MaterialTheme.colorScheme.primaryContainer
                            ContactCategory.PERSONAL -> MaterialTheme.colorScheme.secondaryContainer
                            ContactCategory.BOTH -> MaterialTheme.colorScheme.tertiaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (contact.category) {
                        ContactCategory.WORK -> Icons.Default.BusinessCenter
                        ContactCategory.PERSONAL -> Icons.Default.Home
                        ContactCategory.BOTH -> Icons.Default.Person
                    },
                    contentDescription = null,
                    tint = when (contact.category) {
                        ContactCategory.WORK -> MaterialTheme.colorScheme.onPrimaryContainer
                        ContactCategory.PERSONAL -> MaterialTheme.colorScheme.onSecondaryContainer
                        ContactCategory.BOTH -> MaterialTheme.colorScheme.onTertiaryContainer
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (contact.category) {
                    ContactCategory.WORK -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ContactCategory.PERSONAL -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    ContactCategory.BOTH -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                }
            ) {
                Text(
                    text = contact.category.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (contact.category) {
                        ContactCategory.WORK -> MaterialTheme.colorScheme.primary
                        ContactCategory.PERSONAL -> MaterialTheme.colorScheme.secondary
                        ContactCategory.BOTH -> MaterialTheme.colorScheme.tertiary
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            IconButton(onClick = onEdit) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
