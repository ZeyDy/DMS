import { Button, Table, Space } from "antd";
import { useNavigate } from "react-router-dom";

export const CustomersPage = () => {
  const navigate = useNavigate();

  const columns = [
    {
      title: "Name",
      dataIndex: "name",
    },
    {
      title: "Code",
      dataIndex: "code",
    },
    {
      title: "City",
      dataIndex: "city",
    },
    {
      title: "Actions",
      render: () => <a>Edit</a>,
    },
  ];

  const data = [
    {
      key: 1,
      name: "MB Test",
      code: "123456",
      city: "Klaipėda",
    },
  ];

  return (
    <>
      <Space style={{ marginBottom: 20 }}>
        <Button
          type="primary"
          onClick={() => navigate("/customers/create")}
        >
          Create New Customer
        </Button>
      </Space>

      <Table columns={columns} dataSource={data} />
    </>
  );
};