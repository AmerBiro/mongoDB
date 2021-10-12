package com.androiddevs.data.queries

import com.androiddevs.data.collections.User
import com.noteapp.database.collections.Program
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue

private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("MyFitnessPassMongoDB")
private val programs = database.getCollection<Program>()
private val users = database.getCollection<User>()


suspend fun createProgram(program: Program): Boolean {
    return programs.insertOne(program).wasAcknowledged()
}

suspend fun updateProgram(program: Program): Boolean {
    val programExists = programs.findOneById(program.id) != null
    if (programExists) {
        return programs.updateOneById(program.id, program).wasAcknowledged()
    }
    return false
}

suspend fun shareProgramWithOthers(programId: String, email: String): Boolean {
    // :TODO check if the entered email does not exists in the list
    val program = programs.findOne(Program::id eq programId)
    program?.let { program ->
        // the note has multiple owners, so we just delete the email from the owners list
        val newHasAccess = program.hasAccess + email
        val updateResult = programs.updateOne(Program::id eq program.id, setValue(Program::hasAccess, newHasAccess))
        return updateResult.wasAcknowledged()
    } ?: return false
}

suspend fun getOwnPrograms(owner: String): List<Program> {
    return programs.find(Program::owner eq owner).toList()
}

suspend fun getProgramsSharedWIthMe(email: String): List<Program> {
    return programs.find(Program::hasAccess contains email).toList()
}


suspend fun deleteProgram(owner: String, programId: String): Boolean {
    val program = programs.findOne(Program::owner eq owner, Program::id eq programId)
    program?.let { program ->
        return programs.deleteOneById(program.id).wasAcknowledged()
    } ?: return false
}

suspend fun removeProgramFromSharedWithMeList(programId: String, email: String): Boolean {
    val program = programs.findOne(Program::id eq programId, Program::hasAccess contains email)
    program?.let { program ->
        // the note has multiple owners, so we just delete the email from the owners list
        val newHasAccess = program.hasAccess - email
        val updateResult = programs.updateOne(Program::id eq program.id, setValue(Program::hasAccess, newHasAccess))
        return updateResult.wasAcknowledged()
    } ?: return false
}

suspend fun getFavoritePrograms(owner: String): List<Program> {
    return programs.find(Program::owner eq owner, Program::favoriteStatus eq 1).toList()
}