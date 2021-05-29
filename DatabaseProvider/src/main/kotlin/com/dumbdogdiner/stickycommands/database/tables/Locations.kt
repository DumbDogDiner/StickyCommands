/*
 * Copyright (c) 2021 DumbDogDiner <dumbdogdiner.com>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information.
 */
package com.dumbdogdiner.stickycommands.database.tables

import org.jetbrains.exposed.sql.Table
import com.dumbdogdiner.stickycommands.database.tables.TableVars.prefix
import com.dumbdogdiner.stickycommands.database.tables.TableVars.server

object Locations : Table("$prefix${server}_locations") {

    val uniqueId = varchar("unique_id", 36)

    val world = varchar("world", 36)

    val x = double("x")

    val y = double("y")

    val z = double("z")

    val yaw = float("yaw")

    val pitch = float("pitch")

    override val primaryKey = PrimaryKey(uniqueId)
}
