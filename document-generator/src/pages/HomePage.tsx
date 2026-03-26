import { useEffect, useState } from "react";
import { Table, Button, Tag, Typography, Space, Statistic, Row, Col, Card, notification } from "antd";
import { DownloadOutlined, ReloadOutlined, FileTextOutlined, TeamOutlined, ThunderboltOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import dayjs from "dayjs";

const { Title, Text } = Typography;

interface HistoryRecord {
  id: number;
  companyName: string;
  companyCode: string;
  createdAt: string;
  documentCount: number;
}

export const HomePage = () => {
  const navigate = useNavigate();
  const [history, setHistory] = useState<HistoryRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [downloadingId, setDownloadingId] = useState<number | null>(null);

  const loadHistory = async () => {
    setLoading(true);
    try {
      const res = await fetch("http://localhost:8080/api/documents/history");
      const data = await res.json();
      setHistory(data);
    } catch {
      notification.error({ message: "Error", description: "Failed to load generation history" });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHistory();
  }, []);

  const download = (recordId: number) => {
    setDownloadingId(recordId);
    window.open(`http://localhost:8080/api/documents/download/${recordId}`, "_blank");
    setTimeout(() => setDownloadingId(null), 1500);
  };

  const totalDocuments = history.reduce((sum, r) => sum + r.documentCount, 0);
  const uniqueCompanies = new Set(history.map((r) => r.companyCode)).size;

  const columns = [
    {
      title: "#",
      dataIndex: "id",
      width: 60,
      render: (id: number) => <Text type="secondary">#{id}</Text>,
    },
    {
      title: "Company",
      dataIndex: "companyName",
      render: (name: string, record: HistoryRecord) => (
        <div>
          <Text strong>{name}</Text>
          <br />
          <Text type="secondary" style={{ fontSize: 12 }}>{record.companyCode}</Text>
        </div>
      ),
    },
    {
      title: "Generated At",
      dataIndex: "createdAt",
      render: (date: string) => (
        <div>
          <Text>{dayjs(date).format("YYYY-MM-DD")}</Text>
          <br />
          <Text type="secondary" style={{ fontSize: 12 }}>{dayjs(date).format("HH:mm:ss")}</Text>
        </div>
      ),
      sorter: (a: HistoryRecord, b: HistoryRecord) =>
        dayjs(a.createdAt).unix() - dayjs(b.createdAt).unix(),
      defaultSortOrder: "descend" as const,
    },
    {
      title: "Documents",
      dataIndex: "documentCount",
      render: (count: number) => (
        <Tag color={count > 0 ? "green" : "default"} icon={<FileTextOutlined />}>
          {count} {count === 1 ? "file" : "files"}
        </Tag>
      ),
    },
    {
      title: "Actions",
      render: (_: any, record: HistoryRecord) => (
        <Button
          type="primary"
          size="small"
          icon={<DownloadOutlined />}
          loading={downloadingId === record.id}
          disabled={record.documentCount === 0}
          onClick={() => download(record.id)}
        >
          Download ZIP
        </Button>
      ),
    },
  ];

  return (
    <Space direction="vertical" style={{ width: "100%" }} size="large">
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <Title level={3} style={{ margin: 0 }}>Dashboard</Title>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadHistory} loading={loading}>
            Refresh
          </Button>
          <Button type="primary" icon={<ThunderboltOutlined />} onClick={() => navigate("/documents")}>
            Generate Documents
          </Button>
        </Space>
      </div>

      {/* Stats */}
      <Row gutter={16}>
        <Col span={8}>
          <Card>
            <Statistic
              title="Total Generations"
              value={history.length}
              prefix={<ThunderboltOutlined />}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="Total Documents"
              value={totalDocuments}
              prefix={<FileTextOutlined />}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="Companies Served"
              value={uniqueCompanies}
              prefix={<TeamOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* History table */}
      <Card
        title="Generation History"
        extra={
          <Text type="secondary">
            {history.length} {history.length === 1 ? "record" : "records"}
          </Text>
        }
      >
        <Table
          columns={columns}
          dataSource={history.map((r) => ({ ...r, key: r.id }))}
          loading={loading}
          pagination={{ pageSize: 10, showSizeChanger: true }}
          locale={{ emptyText: "No documents generated yet. Go to Documents to get started." }}
        />
      </Card>
    </Space>
  );
};
