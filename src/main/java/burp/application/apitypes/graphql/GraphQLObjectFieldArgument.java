/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.application.apitypes.graphql;

import burp.application.apitypes.graphql.GraphQLBaseObject;
import burp.application.apitypes.graphql.GraphQLKind;
import burp.application.apitypes.graphql.GraphQLObjectType;
import burp.application.apitypes.graphql.GraphQLParseContext;
import burp.application.apitypes.graphql.GraphQLParseError;
import burp.utils.Constants;
import com.google.gson.JsonObject;

public class GraphQLObjectFieldArgument
        extends GraphQLBaseObject {
    public GraphQLObjectType type;

    public GraphQLObjectFieldArgument(JsonObject inputJson) {
        super(inputJson);
        this.kind = GraphQLKind.OBJECT_FIELD_ARGUMENT;
        this.type = new GraphQLObjectType(inputJson.getAsJsonObject("type"));
    }

    @Override
    public String exportToQuery(GraphQLParseContext context) throws GraphQLParseError {
        String result = "";
        result = result + this.name + ":" + Constants.GRAPHQL_SPACE;
        result = result + context.globalObjects.get(this.type.typeName).exportToQuery(context);
        return result;
    }
}

