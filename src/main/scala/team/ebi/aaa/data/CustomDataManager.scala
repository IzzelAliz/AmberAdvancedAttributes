package team.ebi.aaa.data

import com.google.inject.{Inject, Singleton}
import io.izzel.amber.commons.i18n.AmberLocale
import team.ebi.aaa.util._
import org.slf4j.Logger
import org.spongepowered.api.data.DataRegistration
import org.spongepowered.api.event.game.state.GamePreInitializationEvent
import org.spongepowered.api.plugin.PluginContainer

import scala.util.continuations.reset

@Singleton
class CustomDataManager @Inject()(implicit container: PluginContainer, locale: AmberLocale) {
  reset {
    waitFor[GamePreInitializationEvent]
    locale.log("log.register-data.before")
    registerDoubleRangeSerializer()
    registerCustomTemplates()
  }

  private def registerDoubleRangeSerializer(): Unit = {
    DoubleRange.ensuring(true) // class loading
  }

  private def registerCustomTemplates(): Unit = {
    import team.ebi.aaa.data.CustomTemplates._
    DataRegistration.builder.id(id).name(name)
      .dataClass[Data, ImmutableData](classOf[Data]).immutableClass(classOf[ImmutableData]).builder(Builder).build()
  }
}
