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
package cn.iris.cloud.common.compiler.support;

import java.util.concurrent.atomic.AtomicInteger;

public class JavaCodeTest {

	public final static AtomicInteger SUBFIX = new AtomicInteger(8);

	String getSimpleCode() {
		StringBuilder code = new StringBuilder();
		code.append("package cn.iris.cloud.common.compiler.support;");

		code.append("public class HelloServiceImpl" + SUBFIX.getAndIncrement() + " implements HelloService {");
		code.append("   public String sayHello() { ");
		code.append("       return \"Hello world!\"; ");
		code.append("   }");
		code.append("}");
		return code.toString();
	}

	String getSimpleCodeWithoutPackage() {
		StringBuilder code = new StringBuilder();
		code.append("public class HelloServiceImpl" + SUBFIX.getAndIncrement() + "implements cn.iris.cloud.common.support.compiler.HelloService.HelloService {");
		code.append("   public String sayHello() { ");
		code.append("       return \"Hello world!\"; ");
		code.append("   }");
		code.append("}");
		return code.toString();
	}

	String getSimpleCodeWithSyntax() {
		StringBuilder code = new StringBuilder();
		code.append("package cn.iris.cloud.common.compiler.support;");

		code.append("public class HelloServiceImpl" + SUBFIX.getAndIncrement() + " implements HelloService {");
		code.append("   public String sayHello() { ");
		code.append("       return \"Hello world!\"; ");
		// code.append("   }");
		// }
		return code.toString();
	}

	// only used for javassist
	String getSimpleCodeWithSyntax0() {
		StringBuilder code = new StringBuilder();
		code.append("package cn.iris.cloud.common.compiler.support;");

		code.append("public class HelloServiceImpl_0 implements HelloService {");
		code.append("   public String sayHello() { ");
		code.append("       return \"Hello world!\"; ");
		// code.append("   }");
		// }
		return code.toString();
	}

	String getSimpleCodeWithImports() {
		StringBuilder code = new StringBuilder();
		code.append("package cn.iris.cloud.common.compiler.support;");

		code.append("import java.lang.*;\n");
		code.append("import cn.iris.cloud.common.compiler.support;\n");

		code.append("public class HelloServiceImpl2" + SUBFIX.getAndIncrement() + " implements HelloService {");
		code.append("   public String sayHello() { ");
		code.append("       return \"Hello world!\"; ");
		code.append("   }");
		code.append("}");
		return code.toString();
	}

	String getSimpleCodeWithWithExtends() {
		StringBuilder code = new StringBuilder();
		code.append("package cn.iris.cloud.common.compiler.support;");

		code.append("import java.lang.*;\n");
		code.append("import cn.iris.cloud.common.compiler.support;\n");

		code.append("public class HelloServiceImpl" + SUBFIX.getAndIncrement() + " extends cn.iris.cloud.common.support.compiler.HelloServiceImpl0 {\n");
		code.append("   public String sayHello() { ");
		code.append("       return \"Hello world3!\"; ");
		code.append("   }");
		code.append("}");
		return code.toString();
	}
}
