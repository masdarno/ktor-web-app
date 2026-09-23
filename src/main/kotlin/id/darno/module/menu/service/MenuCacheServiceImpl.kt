package id.darno.module.menu.service

import id.darno.module.menu.domain.MenuDomain
import id.darno.module.menu.repository.MenuRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

class MenuCacheServiceImpl(
    private val menuRepository: MenuRepository
) : MenuCacheService {

    private val logger = LoggerFactory.getLogger(MenuCacheServiceImpl::class.java)
    private val mutex = Mutex()

    @Volatile
    private var initSignal = CompletableDeferred<Unit>()

    @Volatile
    private var allMenus: Map<Short, MenuDomain> = emptyMap()

    private suspend fun awaitInit() {
        initSignal.await()
    }

    override suspend fun init() {
        mutex.withLock {
            if (initSignal.isCompleted) return

            logger.info("Loading menu structure cache...")

            allMenus = menuRepository.findAllMenus()
                .associateBy { it.id }

            initSignal.complete(Unit)

            logger.info("Menu structure loaded: ${allMenus.size} menus")
        }
    }

    override suspend fun getAllMenus(): Collection<MenuDomain> {
        awaitInit()
        return allMenus.values
    }

    override suspend fun reload() {
        mutex.withLock {
            logger.warn("Reloading menu structure cache...")
            initSignal = CompletableDeferred()
            allMenus = emptyMap()
            init()
        }
    }
}