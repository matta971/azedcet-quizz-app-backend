rootProject.name = "mindsoccer-backend"

// Apps
include("apps:api")

// Modules
include("modules:protocol")
include("modules:engine")
include("modules:match")
include("modules:realtime")
include("modules:content")
include("modules:scoring")
include("modules:anticheat")
include("modules:shared")

// Configure module paths
project(":apps:api").projectDir = file("apps/api")
project(":modules:protocol").projectDir = file("modules/protocol")
project(":modules:engine").projectDir = file("modules/engine")
project(":modules:match").projectDir = file("modules/match")
project(":modules:realtime").projectDir = file("modules/realtime")
project(":modules:content").projectDir = file("modules/content")
project(":modules:scoring").projectDir = file("modules/scoring")
project(":modules:anticheat").projectDir = file("modules/anticheat")
project(":modules:shared").projectDir = file("modules/shared")
