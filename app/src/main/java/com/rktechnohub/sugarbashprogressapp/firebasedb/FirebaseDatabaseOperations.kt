package com.rktechnohub.sugarbashprogressapp.firebasedb

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.rktechnohub.sugarbashprogressapp.authentication.model.OrderClass
import com.rktechnohub.sugarbashprogressapp.authentication.model.SessionManager
import com.rktechnohub.sugarbashprogressapp.authentication.model.User
import com.rktechnohub.sugarbashprogressapp.project.model.Project
import com.rktechnohub.sugarbashprogressapp.task.model.TaskModel
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Created by Aiman on 17, May, 2024
 */
class FirebaseDatabaseOperations {
    var userList = mutableListOf<User>()

    //    var projectList = mutableListOf<Project>()
    var taskList = mutableListOf<TaskModel>()
    var subtaskList = mutableListOf<TaskModel>()
    var task: TaskModel? = null
    var project: Project? = null
    var user: User? = null
    var database: FirebaseDatabase = FirebaseDatabase.getInstance()

    fun getUserTableRef(): DatabaseReference {
        return database.reference.child(DBReferences.userTableName)
    }

    fun getProjectTableRef(): DatabaseReference {
        return database.reference.child(DBReferences.projectTableName)
    }

    fun getTaskTableRef(): DatabaseReference {
        return database.reference.child(DBReferences.taskTableName)
    }


    /**
     * ****************************************************************
     * ************************** User *****************************
     * ****************************************************************
     */

    fun saveUserToDb(context: Context) {
        val session = SessionManager(context)
        try {
            val user = session.getUser()
            getUserTableRef().child(user.uid).setValue(user).addOnCompleteListener {

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getUserFromDb(user: User) {
        getUserTableRef().child(user.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    this@FirebaseDatabaseOperations.user = user
                }
                getUserTableRef().removeEventListener(this)
                dataChangedListener?.dataRecieved()
            }

            override fun onCancelled(error: DatabaseError) {
                getUserTableRef().removeEventListener(this)
                dataChangedListener?.canceled()
            }

        })
    }

    fun getAllUsersByRole(role: String, isAdmin: Boolean, project: Project) {
        val ref = if (isAdmin) {
            database.getReference(DBReferences.userTableName)
        } else {
            getUserTableRef().orderByChild(DBReferences.role).equalTo(role)
        }
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (projectSnapshot in snapshot.children) {
                    val user = projectSnapshot.getValue(User::class.java)
                    if (user != null) {
                        if (project.adminId != user.uid &&
                            project.employeeId != user.uid &&
                            project.clientId != user.uid
                        ) {
                            if (isAdmin) {
                                if (user.role == AppUtils.roleAdmin.toString()
                                    || user.role == AppUtils.roleSuperAdmin.toString()
                                ) {
                                    userList.add(user)
                                }
                            } else {
                                userList.add(user)
                            }
                        }
                    }
                }
                getUserTableRef().removeEventListener(this)
                dataChangedListener?.dataRecieved()
            }

            override fun onCancelled(error: DatabaseError) {
                getUserTableRef().removeEventListener(this)
                dataChangedListener?.canceled()
            }

        })
    }


    /**
     * ****************************************************************
     * ************************** Project *****************************
     * ****************************************************************
     */

    fun addProject(project: Project): Project {
        val newRef = getProjectTableRef().push()
        project.id = newRef.key!!
        newRef.setValue(project)
        return project
    }

    fun updateProject(project: Project) {
        val ref = getProjectTableRef().child(project.id)
        ref.setValue(project)
    }

    suspend fun deleteProject(project: Project, callback: () -> Unit) {
        if (project.taskId.isNotEmpty()) {
            if (project.taskId.contains(",")){
                val taskIds = project.taskId.split(",")
                for (id in taskIds){
                    deleteTask(getTaskByTaskIdCoroutine(id)){
                        val database = FirebaseDatabase.getInstance()
                        val ref = database.getReference("${DBReferences.projectTableName}/${project.id}")

                        ref.removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("Firebase", "Item deleted successfully")
                                deleteDataListener?.success()
                                callback()
                            } else {
                                Log.d("Firebase", "Error deleting item: ${task.exception?.message}")
                                deleteDataListener?.failed()
                            }
                        }
                    }
                }
            } else {
                   try {
                       deleteTask(getTaskByTaskIdCoroutine(project.taskId)){
                           val database = FirebaseDatabase.getInstance()
                           val ref = database.getReference("${DBReferences.projectTableName}/${project.id}")

                           ref.removeValue().addOnCompleteListener { task ->
                               if (task.isSuccessful) {
                                   Log.d("Firebase", "Item deleted successfully")
                                   deleteDataListener?.success()
                                   callback()
                               } else {
                                   Log.d("Firebase", "Error deleting item: ${task.exception?.message}")
                                   deleteDataListener?.failed()
                               }
                           }
                       }
                   } catch (e: Exception){

                   }
            }

        } else {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference("${DBReferences.projectTableName}/${project.id}")

            ref.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Firebase", "Item deleted successfully")
                    deleteDataListener?.success()
                    callback()
                } else {
                    Log.d("Firebase", "Error deleting item: ${task.exception?.message}")
                    deleteDataListener?.failed()
                }
            }
        }

    }

    //TODO only once called
    suspend fun updateAllProjectOrder() {
        val ref = FirebaseDatabase.getInstance().getReference(DBReferences.projectTableName)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEachIndexed { _, itemSnapshot ->
                    val itemId = itemSnapshot.key
                    val itemData = itemSnapshot.getValue<Map<String, Any>>()?.toMutableMap()
                    itemData!![DBReferences.oldMapLink] = ""// add new field
                    ref.child(itemId!!).updateChildren(itemData)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Error", "Error updating items", databaseError.toException())
            }
        })

        /*  val listProjects = getAllProjectsForSuperAdminCoroutine()
          listProjects.forEachIndexed { index, project ->
              project.order = index.toString()
              project.mapLink = ""
              val ref = getProjectTableRef().child(project.id)
              ref.setValue(project)
          }*/
    }

    fun getProjectByProjectId(projectId: String) {
        getProjectTableRef().orderByChild(DBReferences.id).equalTo(projectId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (projectSnapshot in snapshot.children) {
                        project = projectSnapshot.getValue(Project::class.java)
                    }
                    getProjectTableRef().removeEventListener(this)
                    dataChangedListener?.dataRecieved()
                }

                override fun onCancelled(error: DatabaseError) {
                    getProjectTableRef().removeEventListener(this)
                    dataChangedListener?.canceled()
                }

            })
    }

    suspend fun getAllProjectsForSuperAdminCoroutine(list: List<OrderClass>): List<Project> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                val ref = database.getReference(DBReferences.projectTableName)
                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val projectList = mutableListOf<Project>()

                        for (projectSnapshot in snapshot.children) {
                            val projectMap = projectSnapshot.value as Map<String, Any>
                            val project = fromMap(projectMap)
                            projectList.add(project)
                        }

                        ref.removeEventListener(this)
                        continuation.resume(orderBy(projectList, list))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        ref.removeEventListener(this)
                        continuation.resumeWithException(error.toException())
                    }
                }

                ref.addListenerForSingleValueEvent(listener)

                // Cancel the listener when the coroutine is cancelled
                continuation.invokeOnCancellation { ref.removeEventListener(listener) }
            }
        }
    }

    /* fun getAllProjectsForSuperAdmin() {
         val ref = database.getReference(DBReferences.projectTableName)
         ref.addValueEventListener(object : ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 Log.e("value", snapshot.value.toString())
                 Log.e("child", snapshot.children.toString())


                 for (projectSnapshot in snapshot.value as Map<String, Any>) {
                     val projectMap = projectSnapshot.value as Map<String, Any>
                     val project = fromMap(projectMap)
                     projectList.add(project)
                 }
                 ref.removeEventListener(this)
                 dataChangedListener?.dataRecieved()
             }

             override fun onCancelled(error: DatabaseError) {
                 ref.removeEventListener(this)
                 dataChangedListener?.canceled()
             }

         })
     }*/

    suspend fun getAllProjectsForAdminCoroutine(adminId: String, list: List<OrderClass>): List<Project> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->

                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val projList: MutableList<Project> = mutableListOf()
                        for (projectSnapshot in snapshot.children) {
                            val project = projectSnapshot.getValue(Project::class.java)
                            if (project != null) {
                                projList.add(project)
                            }
                        }
                        getProjectTableRef().removeEventListener(this)
                        continuation.resume(orderBy(projList, list))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        getProjectTableRef().removeEventListener(this)
                        continuation.resumeWithException(error.toException())

                    }

                }

                getProjectTableRef().orderByChild(DBReferences.adminId).equalTo(adminId)
                    .addValueEventListener(listener)
                continuation.invokeOnCancellation {
                    getProjectTableRef().removeEventListener(
                        listener
                    )
                }
            }
        }
        /* getProjectTableRef().orderByChild(DBReferences.adminId).equalTo(adminId)
             .addValueEventListener(object : ValueEventListener{
             override fun onDataChange(snapshot: DataSnapshot) {
                 projectList.clear()
                 for (projectSnapshot in snapshot.children) {
                     val project = projectSnapshot.getValue(Project::class.java)
                     if (project != null) {
                         projectList.add(project)
                     }
                 }
                 getProjectTableRef().removeEventListener(this)
                 dataChangedListener?.dataRecieved()
             }

             override fun onCancelled(error: DatabaseError) {
                 getProjectTableRef().removeEventListener(this)
                 dataChangedListener?.canceled()
             }

         })*/
    }

    suspend fun getAllProjectsForEmployee(empId: String, list: List<OrderClass>): List<Project> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->

                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val projectList: MutableList<Project> = mutableListOf()
                        for (projectSnapshot in snapshot.children) {
                            val project = projectSnapshot.getValue(Project::class.java)
                            if (project != null) {
                                projectList.add(project)
                            }
                        }
                        getProjectTableRef().removeEventListener(this)
                        continuation.resume(orderBy(projectList, list))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        getProjectTableRef().removeEventListener(this)
                        continuation.resumeWithException(error.toException())

                    }

                }

                getProjectTableRef().orderByChild(DBReferences.employeeId).equalTo(empId)
                    .addValueEventListener(listener)
                continuation.invokeOnCancellation {
                    getProjectTableRef().removeEventListener(
                        listener
                    )
                }
            }
        }

    }

    suspend fun getAllProjectsForClient(clientId: String, list: List<OrderClass>): List<Project> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->

                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val projectList: MutableList<Project> = mutableListOf()
                        for (projectSnapshot in snapshot.children) {
                            val project = projectSnapshot.getValue(Project::class.java)
                            if (project != null) {
                                projectList.add(project)
                            }
                        }
                        getProjectTableRef().removeEventListener(this)
                        continuation.resume(orderBy(projectList, list))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        getProjectTableRef().removeEventListener(this)
                        continuation.resumeWithException(error.toException())
                    }

                }

                getProjectTableRef().orderByChild(DBReferences.clientId).equalTo(clientId)
                    .addValueEventListener(listener)
                continuation.invokeOnCancellation {
                    getProjectTableRef().removeEventListener(
                        listener
                    )
                }
            }
        }
    }


    suspend fun deleteImageFromFirebaseStorage(link: String) {
        return withContext(Dispatchers.IO) {
            val storageReference = FirebaseStorage.getInstance().reference
                .storage.getReferenceFromUrl(link)
            storageReference.delete().await()
        }
    }
    suspend fun uploadBitmapToFirebaseStorage(bitmap: Bitmap): Uri? {
        return withContext(Dispatchers.IO) {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("maplink/${System.currentTimeMillis()}.jpg")

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val uploadTask = imageRef.putBytes(data)
            uploadTask.await()

            imageRef.downloadUrl.await()
        }
    }


    fun orderBy(list: List<Project>, orderList: List<OrderClass>): List<Project> {
        val orderedProjects = list.filter { project -> orderList.any { it.id == project.id } }
        val unorderedProjects = list.filter { project -> !orderList.any { it.id == project.id } }

        val sortedList = orderedProjects.sortedBy { project ->
            val order = orderList.find { it.id == project.id }?.order
            order
        }

        return unorderedProjects + sortedList
    }

    suspend fun copyProject(project: Project, orderList: List<OrderClass>): Project{
       try{
           var nProject = Project(
               "", project.name + "-Copy", project.icon, AppUtils.getCurrentDate(), "", "", "","",
               project.taskId,  project.adminId, project.employeeId,project.clientId, "0", "", "", ""
           )
           nProject = addProject(nProject)
           val taskList = getTaskByProjectIdCoroutine(project.id, true, orderList)
           val taskIDs: MutableList<String> = mutableListOf()
           for (task in taskList){
//               var nTask = task
               var nTask = TaskModel("", task.name + "-Copy", task.icon,
                   "", task.id, "", "0", task.userId,
                   nProject.id, "0", "", "")
               nTask = addTask(nTask)
               val subTaskIDs: MutableList<String> = mutableListOf()
               if (task.taskId.isNotEmpty()){
                   val subTasks: MutableList<TaskModel> = mutableListOf()
                   if (task.taskId.contains(",")){
                       val ids = task.taskId.split(",")
                       for (id in ids){
                           val subTask = getTaskByTaskIdCoroutine(id)
//                           var nSubTask = subTask
                           var nSubTask = TaskModel("", subTask.name + "-Copy", subTask.icon,
                               "", "", nTask.id, "0", subTask.userId,
                               nProject.id, "0", "", "")
                           nSubTask = addTask(nSubTask)
                           subTaskIDs.add(nSubTask.id)
                           subTasks.add(nSubTask)
                       }
                   } else {
                       val subTask = getTaskByTaskIdCoroutine(task.taskId)
                       var nSubTask = TaskModel("", subTask.name + "-Copy", subTask.icon,
                           "", "", nTask.id, "0", subTask.userId,
                           nProject.id, "0", "", "")
                       nSubTask = addTask(nSubTask)
                       subTaskIDs.add(nSubTask.id)
                       subTasks.add(nSubTask)
                   }


//                   task.id = nTask.id

                   nTask.taskId = subTaskIDs.joinToString (", ")
                   updateTask(nTask)
                   taskIDs.add(nTask.id)
                   nTask.subTaskList = subTasks
               }
           }
           return withContext(Dispatchers.IO) {
               suspendCancellableCoroutine { continuation ->
                   nProject.taskId = taskIDs.joinToString (", ")
                   nProject.taskList.addAll(taskList)
                   updateProject(nProject)
                   continuation.resume(nProject)
               }
           }
       } catch (e: Exception){
           return project
       }
    }


    /**
     * ****************************************************************
     * ************************** Tasks *****************************
     * ****************************************************************
     */

    fun addTask(task: TaskModel): TaskModel {
        val newRef = getTaskTableRef().push()
        task.id = newRef.key!!
        newRef.setValue(task)
        return task
    }

    fun updateTask(task: TaskModel) {
        val ref = getTaskTableRef().child(task.id)
        ref.setValue(task)
    }

    suspend fun deleteTask(tasks: TaskModel, callback: () -> Unit) {
        if (tasks.taskId.isNotEmpty()) {
            try {
                deleteSubTasks(tasks) {
                    val database = FirebaseDatabase.getInstance()
                    val ref = database.getReference("${DBReferences.taskTableName}/${tasks.id}")

                    ref.removeValue().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("Firebase", "Item deleted successfully")
                            deleteDataListener?.success()
                            callback()
                        } else {
                            Log.d("Firebase", "Error deleting item: ${task.exception?.message}")
                            deleteDataListener?.failed()
                        }
                    }
                }
            } catch (e: Exception){
                val database = FirebaseDatabase.getInstance()
                val ref = database.getReference("${DBReferences.taskTableName}/${tasks.id}")

                ref.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Firebase", "Item deleted successfully")
                        deleteDataListener?.success()
                        callback()
                    } else {
                        Log.d("Firebase", "Error deleting item: ${task.exception?.message}")
                        deleteDataListener?.failed()
                    }
                }
            }
        } else {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference("${DBReferences.taskTableName}/${tasks.id}")

            ref.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Firebase", "Item deleted successfully")
                    deleteDataListener?.success()
                    callback()
                } else {
                    Log.d("Firebase", "Error deleting item: ${task.exception?.message}")
                    deleteDataListener?.failed()
                }
            }
        }

    }

    /*suspend fun deleteTask(tasks: TaskModel) = suspendCancellableCoroutine<Unit> { continuation ->
        if (tasks.taskId.isNotEmpty()) {
            deleteSubTasks(tasks) {
                val database = FirebaseDatabase.getInstance()
                val ref = database.getReference("${DBReferences.taskTableName}/${tasks.id}")

                ref.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Firebase", "Item deleted successfully")
                        continuation.resume(Unit)
                    } else {
                        Log.d("Firebase", "Error deleting item: ${task.exception?.message}")
                        continuation.resumeWithException(task.exception!!)
                    }
                }
            }
        } else {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference("${DBReferences.taskTableName}/${tasks.id}")

            ref.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Firebase", "Item deleted successfully")
                    continuation.resume(Unit)
                } else {
                    Log.d("Firebase", "Error deleting item: ${task.exception?.message}")
                    continuation.resumeWithException(task.exception!!)
                }
            }
        }
    }*/

    suspend fun deleteSubTasks(task: TaskModel, callback: () -> Unit) = coroutineScope {
        val taskIds = task.taskId.split(",")
        val tasksToDelete = mutableListOf<TaskModel>()

        taskIds.forEach { id ->
            val taskModel = getTaskByTaskIdCoroutine(id)
            tasksToDelete.add(taskModel)

        }

        tasksToDelete.forEach { deleteTask(it) {} }
        callback()
    }

    fun updateTask(task: TaskModel, isInitialUpdate: Boolean) {
        val ref = getTaskTableRef().child(task.id)
        ref.setValue(task)
    }

    fun getTaskByProjectId(projectId: String, isInitialUpdate: Boolean = true) {
        getTaskTableRef().orderByChild(DBReferences.projectId).equalTo(projectId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    taskList.clear()
                    for (projectSnapshot in snapshot.children) {
                        val task = projectSnapshot.getValue(TaskModel::class.java)
                        if (task != null && task.mainTaskId.isEmpty()) {
                            taskList.add(task)
                        }
                    }
                    if (isInitialUpdate) {
                        dataChangedListener?.dataRecieved()
                    }
                    getTaskTableRef().removeEventListener(this)
                }

                override fun onCancelled(error: DatabaseError) {
                    dataChangedListener?.canceled()
                    getTaskTableRef().removeEventListener(this)
                }

            })
    }

    suspend fun getTaskByProjectIdCoroutine(
        projectId: String,
        isInitialUpdate: Boolean = true, list: List<OrderClass>
    ): List<TaskModel> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->

                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        taskList.clear()
                        for (projectSnapshot in snapshot.children) {
                            val task = projectSnapshot.getValue(TaskModel::class.java)
                            if (task != null && task.mainTaskId.isEmpty()) {
                                taskList.add(task)
                            }
                        }
                        if (isInitialUpdate) {
                            continuation.resume(orderByTask(taskList, list))
                        }
                        getTaskTableRef().removeEventListener(this)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        getTaskTableRef().removeEventListener(this)
                        continuation.resumeWithException(error.toException())
                    }

                }
                getTaskTableRef().orderByChild(DBReferences.projectId).equalTo(projectId)
                    .addValueEventListener(listener)
                continuation.invokeOnCancellation {
                    getProjectTableRef().removeEventListener(
                        listener
                    )
                }

            }
        }
    }


    suspend fun getTaskByUserId(userId: String, list: List<OrderClass>): List<TaskModel> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                val ref = database.getReference(DBReferences.taskTableName)
                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        taskList.clear()
                        for (projectSnapshot in snapshot.children) {
                            val task = projectSnapshot.getValue(TaskModel::class.java)
                            if (task != null && task.userId.contains(userId)) {
                                taskList.add(task)
                            }
                        }
                        continuation.resume(orderByTask(taskList, list))
                        ref.removeEventListener(this)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        ref.removeEventListener(this)
                        continuation.resumeWithException(error.toException())

                    }

                }
                ref
                    .addValueEventListener(listener)
                continuation.invokeOnCancellation {
                    getProjectTableRef().removeEventListener(
                        listener
                    )
                }


            }
        }
    }

    suspend fun getTaskByUserIdDate(userId: String, list: List<OrderClass>): List<TaskModel> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                val ref = database.getReference(DBReferences.taskTableName)
                val listener = object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        taskList.clear()
                        for (projectSnapshot in snapshot.children) {
                            val task = projectSnapshot.getValue(TaskModel::class.java)
                            if (task != null && task.userId.contains(userId) &&
                                task.deadline_date.isNotEmpty() && AppUtils.isPastDate(task.deadline_date)
                            ) {
                                taskList.add(task)
                            }
                        }
                        ref.removeEventListener(this)
                        continuation.resume(orderByTask(taskList, list))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        ref.removeEventListener(this)
                        continuation.resumeWithException(error.toException())

                    }

                }
                ref.addValueEventListener(listener)
                continuation.invokeOnCancellation {
                    getProjectTableRef().removeEventListener(
                        listener
                    )
                }

            }
        }
    }

    suspend fun getTaskByMainTaskId(taskId: String, list: List<OrderClass>): List<TaskModel> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->

                val listener = object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        subtaskList.clear()
                        for (projectSnapshot in snapshot.children) {
                            val task = projectSnapshot.getValue(TaskModel::class.java)
                            if (task != null) {
                                subtaskList.add(task)
                            }
                        }
                        getTaskTableRef().removeEventListener(this)
                        continuation.resume(orderByTask(subtaskList, list))

                    }

                    override fun onCancelled(error: DatabaseError) {
                        getTaskTableRef().removeEventListener(this)
                        continuation.resumeWithException(error.toException())

                    }

                }
                getTaskTableRef().orderByChild(DBReferences.mainTaskId).equalTo(taskId)
                    .addValueEventListener(listener)
                continuation.invokeOnCancellation {
                    getProjectTableRef().removeEventListener(
                        listener
                    )
                }

            }
        }

    }


    fun getTaskByTaskId(taskId: String) {
        getTaskTableRef().orderByChild(DBReferences.id).equalTo(taskId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (projectSnapshot in snapshot.children) {
                        task = projectSnapshot.getValue(TaskModel::class.java)
                    }
                    getTaskTableRef().removeEventListener(this)
                    dataChangedListener?.dataRecieved()
                }

                override fun onCancelled(error: DatabaseError) {
                    dataChangedListener?.canceled()
                    getTaskTableRef().removeEventListener(this)
                }

            })
    }

    suspend fun getTaskByTaskIdCoroutine(taskId: String): TaskModel =
        suspendCancellableCoroutine { continuation ->
            getTaskTableRef().orderByChild(DBReferences.id).equalTo(taskId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (projectSnapshot in snapshot.children) {
                            val taskModel = projectSnapshot.getValue(TaskModel::class.java)
                            continuation.resume(taskModel!!)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
        }

    suspend fun getTaskByProjectIdFlow(projectId: String, isInitialUpdate: Boolean = true)
            : MutableList<TaskModel> = suspendCancellableCoroutine { continuation ->
        getTaskTableRef().orderByChild(DBReferences.projectId).equalTo(projectId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    taskList.clear()
                    for (projectSnapshot in snapshot.children) {
                        val task = projectSnapshot.getValue(TaskModel::class.java)
                        if (task != null && task.mainTaskId.isEmpty()) {
                            taskList.add(task)
                        }
                    }
                    /* if (isInitialUpdate) {
                         dataChangedListener?.dataRecieved()
                     }*/
                    getTaskTableRef().removeEventListener(this)
                    continuation.resume(taskList)
                }

                override fun onCancelled(error: DatabaseError) {
//                    dataChangedListener?.canceled()
                    getTaskTableRef().removeEventListener(this)
                    continuation.resumeWithException(error.toException())
                }

            })
    }

        fun orderByTask(list: List<TaskModel>, orderList: List<OrderClass>): List<TaskModel> {
            val orderedTaskModel = list.filter { task -> orderList.any { it.id == task.id } }
            val unorderedTaskModel = list.filter { task -> !orderList.any { it.id == task.id } }

            val sortedList = orderedTaskModel.sortedBy { task ->
                val order = orderList.find { it.id == task.id }?.order
                order
            }

            return unorderedTaskModel + sortedList
        }



    object DBReferences {
        const val userTableName = "User"
        const val projectTableName = "Project"
        const val taskTableName = "Task"

        //raws
        const val id = "id"
        const val role = "role"
        const val adminId = "adminId"
        const val employeeId = "employeeId"
        const val clientId = "clientId"
        const val projectId = "projectId"
        const val userId = "userId"
        const val mainTaskId = "mainTaskId"
        const val taskId = "taskId"
        const val deadline_date = "deadline_date"
        const val mapLink = "mapLink"
        const val oldMapLink = "oldMapLink"
        const val order = "order"
    }

    var dataChangedListener: DataChangedListener? = null

    fun addOnDataChangedListener(onDataChangedListener: DataChangedListener) {
        this.dataChangedListener = onDataChangedListener
    }

    interface DataChangedListener {
        fun dataRecieved()
        fun canceled()
    }

    var deleteDataListener: DeleteDataListener? = null

    fun addOnDeleteDataListener(deleteDataListener: DeleteDataListener) {
        this.deleteDataListener = deleteDataListener
    }

    interface DeleteDataListener {
        fun success()
        fun failed()
    }


    /********************************Commmon***************************************
     * ***************************************************************************
     * *****************************************************************************
     */

    fun fromMap(map: Map<String, Any>): Project {
        return Project(
            map["id"] as String,
            map["name"] as String,
            map["icon"] as String,
            map["startDate"] as String,
            map["endDate"] as String,
            map["daysLeft"] as String,
            map["target"] as String,
            map["description"] as String,
            map["taskId"] as String,
            map["adminId"] as String,
            map["employeeId"] as String,
            map["clientId"] as String,
            map["progress"] as String,
            map["mapLink"] as String,
            map["oldMapLink"] as String,
            map["order"] as String,
        )
    }
}