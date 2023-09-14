/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.iris.cloud.common.extension.ext8_add.impl;

import cn.iris.cloud.common.URL;
import cn.iris.cloud.common.extension.Adaptive;
import cn.iris.cloud.common.extension.ExtensionLoader;
import cn.iris.cloud.common.extension.ext8_add.AddExt2;

@Adaptive
public class AddExt2_ManualAdaptive implements AddExt2 {
	public String echo(URL url, String s) {
		AddExt2 addExt1 = ExtensionLoader.getExtensionLoader(AddExt2.class).getExtension(url.getParameter("add.ext2"));
		return addExt1.echo(url, s);
	}
}