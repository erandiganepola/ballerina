/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.lang.xmls;

import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.jaxen.JaxenException;
import org.jaxen.XPathSyntaxException;

import java.util.ArrayList;
import java.util.List;

/**
 * Remove the element(s) that matches the given xPath. Namespaces are supported.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.xmls",
        functionName = "removeWithNamespace",
        args = {@Argument(name = "x", type = TypeKind.XML),
                @Argument(name = "xPath", type = TypeKind.STRING),
                @Argument(name = "namespaces", type = TypeKind.MAP)},
        isPublic = true
)
public class RemoveWithNamespaces extends AbstractNativeFunction {

    private static final String OPERATION = "remove element from xml";

    @Override
    public BValue[] execute(Context ctx) {
        try {
            // Accessing Parameters.
            BXML xml = (BXML) getRefArgument(ctx, 0);
            String xPath = getStringArgument(ctx, 0);
            BMap<String, BString> namespaces = (BMap) getRefArgument(ctx, 1);

            xml = XMLUtils.getSingletonValue(xml);
            
            // Setting the value to XML
            AXIOMXPath axiomxPath = new AXIOMXPath(xPath);
            if (namespaces != null && !namespaces.isEmpty()) {
                for (String entry : namespaces.keySet()) {
                    axiomxPath.addNamespace(entry, namespaces.get(entry).stringValue());
                }
            }
            Object ob = axiomxPath.evaluate(xml.value());
            if (ob instanceof ArrayList) {
                List<?> list = (List<?>) ob;

                for (Object obj : list) {
                    if (obj instanceof OMNode) {
                        OMNode omNode = (OMNode) obj;
                        omNode.detach();

                    }
                }
            }
        } catch (XPathSyntaxException e) {
            ErrorHandler.handleInvalidXPath(OPERATION, e);
        } catch (JaxenException e) {
            ErrorHandler.handleXMLException(OPERATION, e);
        } catch (Throwable e) {
            ErrorHandler.handleXMLException(OPERATION, e);
        }

        return VOID_RETURN;
    }
}
