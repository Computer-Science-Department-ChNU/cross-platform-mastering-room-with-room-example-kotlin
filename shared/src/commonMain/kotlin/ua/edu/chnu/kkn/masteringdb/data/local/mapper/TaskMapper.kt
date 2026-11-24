package ua.edu.chnu.kkn.masteringdb.data.local.mapper

import ua.edu.chnu.kkn.masteringdb.data.local.entity.TaskEntity
import ua.edu.chnu.kkn.masteringdb.data.model.Task

fun TaskEntity.toModel(): Task = Task(
    id = id,
    title = title,
    description = description,
    isDone = isDone,
    createdAt = createdAt,
)
