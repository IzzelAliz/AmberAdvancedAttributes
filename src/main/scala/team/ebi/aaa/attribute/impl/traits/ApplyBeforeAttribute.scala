package team.ebi.aaa.attribute.impl.traits

import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.projectile.Projectile
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource
import org.spongepowered.api.event.entity.DamageEntityEvent
import org.spongepowered.api.plugin.PluginContainer
import team.ebi.aaa.api.data.{Mappings, TemplateSlot}
import team.ebi.aaa.util.{listenTo, _}

import scala.annotation.tailrec
import scala.util.Random

trait ApplyBeforeAttribute extends DoubleRangeAttribute {
  implicit def pluginContainer: PluginContainer

  @tailrec
  // noinspection DuplicatedCode
  private def getRealEntity(target: AnyRef): Option[Entity] = target match {
    case entity: Projectile => getRealEntity(entity.getShooter)
    case entity: Entity => Some(entity)
    case _ => None
  }

  def getMappings(source: Entity, target: Entity): Iterable[(TemplateSlot, Mappings)]

  def on(event: DamageEntityEvent): Unit = {
    for (source <- event.getCause.first(classOf[EntityDamageSource]).asScala; entity <- getRealEntity(source.getSource)) {
      for ((_, mappings) <- getMappings(entity, event.getTargetEntity)) {
        val dataStream = Mappings.flattenDataStream(mappings, this)
        for (data <- dataStream.iterator.asScala) {
          event.setBaseDamage(data(Random)(event.getBaseDamage))
        }
      }
    }
  }
}