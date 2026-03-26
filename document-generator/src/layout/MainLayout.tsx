import { Layout, Menu } from "antd";
import { Outlet, useNavigate } from "react-router-dom";

const { Header, Sider, Content } = Layout;

export const MainLayout = () => {
  const navigate = useNavigate();

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider>
        <Menu
          theme="dark"
          mode="inline"
          onClick={({ key }) => navigate(key)}
          items={[
            { key: "/", label: "Home Page" },
            { key: "/customers", label: "Clients" },
            { key: "/documents", label: "Documents" },
          ]}
        />
      </Sider>

      <Layout>
        <Header
          style={{
            color: "white",
            fontSize: 18,
          }}
        >
          Document Generator
        </Header>

        <Content style={{ padding: 24 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};
