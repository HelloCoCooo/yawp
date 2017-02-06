package io.yawp.tools;

import io.yawp.commons.http.RequestContext;
import io.yawp.tools.datastore.DeleteAllTool;
import io.yawp.tools.pipes.FlowPipeDropsTool;
import io.yawp.tools.pipes.ReloadPipeTool;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.yawp.repository.Yawp.yawp;

public class ToolsServlet extends HttpServlet {

    protected Map<String, Class<? extends Tool>> routes = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        routes.put("/datastore/delete-all", DeleteAllTool.class);
        routes.put("/pipes/reload", ReloadPipeTool.class);
        routes.put("/pipes/flow-drops", FlowPipeDropsTool.class);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestContext ctx = new RequestContext(req, resp);
        String path = ctx.getUri();

        if (!routes.containsKey(path)) {
            resp.setStatus(404);
            return;
        }
        execute(routes.get(path), ctx);
    }

    private String getToolPath(HttpServletRequest req) {
        return req.getRequestURI().substring(req.getServletPath().length());
    }

    private void execute(Class<? extends Tool> toolClazz, RequestContext ctx) throws IOException {
        Tool tool = newToolInstance(toolClazz, ctx);
        tool.prepareAndExecute();
    }

    private Tool newToolInstance(Class<? extends Tool> toolClazz, RequestContext ctx) {
        try {
            Tool tool = toolClazz.newInstance();
            tool.setRepository(yawp().setRequestContext(ctx));
            return tool;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
