package com.UD.hideandseek.ECS

interface Component

class ECS {

    class Entity {
        private val components = mutableMapOf<Class<out Component>, Component>()

        // 1. Forzamos a que guarde el componente usando el tipo genérico T explícito
        fun <T : Component> add(c: T, cls: Class<T>): Entity {
            components[cls] = c
            return this
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : Component> get(cls: Class<T>): T? = components[cls] as? T

        fun <T : Component> has(cls: Class<T>) = components.containsKey(cls)

        // --- EXTENSIONES MODERNAS PARA KOTLIN (Opcional, pero recomendadas) ---
        // Te permiten usar entity.get<TemperatureComponent>() en lugar de entity.get(TemperatureComponent::class.java)
        inline fun <reified T : Component> add(c: T): Entity = add(c, T::class.java)
        inline fun <reified T : Component> get(): T? = get(T::class.java)
        inline fun <reified T : Component> has(): Boolean = has(T::class.java)
    }

    interface System {
        fun update(entities: List<Entity>, dt: Float)
    }

    class World {
        val entities = mutableListOf<Entity>()
        private val systems = mutableListOf<System>()
        fun addSystem(s: System) = systems.add(s)
        fun addEntity(e: Entity): Entity { entities.add(e); return e }
        fun update(dt: Float) { systems.forEach { it.update(entities, dt) } }
    }
}
