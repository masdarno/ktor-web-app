package id.darno.module.role.service

import id.darno.module.role.repository.RoleMenuRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

class RoleMenuCacheServiceImpl(
    private val roleMenuRepository: RoleMenuRepository
) : RoleMenuCacheService {

    private val logger = LoggerFactory.getLogger(RoleMenuCacheServiceImpl::class.java)
    private val mutex = Mutex()

    @Volatile
    private var initSignal = CompletableDeferred<Unit>()

    @Volatile
    private var roleMenus: Map<Short, Set<Short>> = emptyMap()

    private suspend fun awaitInit() {
        initSignal.await()
    }

    override suspend fun init() {
        mutex.withLock {
            if (initSignal.isCompleted) return

            logger.info("Loading role-menu ACL cache...")

            roleMenus = roleMenuRepository.findAllRoleMenus()

            initSignal.complete(Unit)

            logger.info("Role-menu cache loaded: ${roleMenus.size} roles")
        }
    }

    override suspend fun getMenuIdsByRole(roleId: Short): Set<Short> {
        awaitInit()
        return roleMenus[roleId] ?: emptySet()
    }

    override suspend fun reload(roleId: Short?) {
        mutex.withLock {
            when (roleId) {
                null -> {
                    logger.warn("Reloading ALL role-menu cache...")
                    initSignal = CompletableDeferred()
                    roleMenus = emptyMap()
                    init()
                }
                else -> {
                    logger.info("Reloading role-menu cache for roleId={}", roleId)
                    roleMenus = roleMenus.toMutableMap().apply {
                        put(roleId, roleMenuRepository.findMenuIdsByRole(roleId))
                    }
                }
            }
        }
    }
}