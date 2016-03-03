package org.scalacourse

import com.synergygb.zordon.common.storage.JsonKeyValueStoreContext
import com.synergygb.zordon.core.Configurable

/**
  * Created by Sebastian on 2/27/2016.
  */
object Context extends JsonKeyValueStoreContext with Configurable
