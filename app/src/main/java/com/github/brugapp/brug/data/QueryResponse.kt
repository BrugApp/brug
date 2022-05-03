package com.github.brugapp.brug.data

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

//TODO: MAKE SENSE OF onCancellation PARAMETER
data class QueryResponse(val packet: QuerySnapshot?, val error: FirebaseFirestoreException?)
