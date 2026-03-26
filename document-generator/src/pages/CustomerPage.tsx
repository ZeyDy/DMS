import { Button, Table, Space, Popconfirm, notification } from "antd";
import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";

export const CustomersPage = () => {
  const navigate = useNavigate();
  const [companies, setCompanies] = useState([]);
  const [loadingId, setLoadingId] = useState<number | null>(null);
  const [generationRecords, setGenerationRecords] = useState<Record<number, number>>({});

const generateDocuments = async (companyId: number) => {
  setLoadingId(companyId);
  try {
    const res = await fetch(
      `http://localhost:8080/api/documents/generate-package/company/${companyId}`,
      { method: "POST" }
    );

    const data = await res.json();

    if (!res.ok) throw new Error(data);

    setGenerationRecords(prev => ({ ...prev, [companyId]: data.generationRecordId })); // ✅ išsaugom recordId

    notification.success({
      message: "Success",
      description: "Documents generated successfully",
    });
  } catch (e: any) {
    notification.error({
      message: "Error",
      description: e.message || "Failed to generate documents",
    });
  } finally {
    setLoadingId(null);
  }
};

const downloadDocuments = (recordId: number) => {
  window.open(
    `http://localhost:8080/api/documents/download/${recordId}`,
    "_blank"
  );
};



  const loadCompanies = () => {
    fetch("http://localhost:8080/api/companies")
      .then((res) => res.json())
      .then((data) => {
        const formatted = data.map((c: any) => ({
          key: c.id,
          id: c.id,
          type: c.type,
          name: c.name,
          code: c.code,
          city: c.cityOrDistrict,
          managerType: c.managerType,
          managerName: c.managerFullName,
        }));

        setCompanies(formatted);
      });
  };

  useEffect(() => {
    loadCompanies();
  }, []);

  const deleteCompany = async (id: number) => {
    await fetch(`http://localhost:8080/api/companies/${id}`, {
      method: "DELETE",
    });

    loadCompanies();
  };

  const columns = [
    {
      title: "Name",
      render: (record: any) => `${record.type} ${record.name}`,
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
      title: "Manager",
      render: (record: any) =>
        `${record.managerType} ${record.managerName}`,
    },
 {
  title: "Actions",
  render: (_: any, record: any) => (
    <Space>
      <a onClick={() => navigate(`/customers/edit/${record.id}`)}>Edit</a>

      <Popconfirm
        title="Delete customer?"
        onConfirm={() => deleteCompany(record.id)}
      >
        <a style={{ color: "red" }}>Delete</a>
      </Popconfirm>

      <Popconfirm
        title="Generate documents for this company?"
        onConfirm={() => generateDocuments(record.id)}
      >
        <Button loading={loadingId === record.id}>Generate Docs</Button>
      </Popconfirm>

      {generationRecords[record.id] && (
  <Button type="primary" onClick={() => downloadDocuments(generationRecords[record.id])}>
    Download ZIP
  </Button>
)}
    </Space>
  ),
}
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

      <Table columns={columns} dataSource={companies} />
    </>
  );
};