import { useEffect, useState } from "react";
import {
  Card,
  Select,
  Button,
  Space,
  notification,
  Checkbox,
  Divider,
  Tag,
  Typography,
} from "antd";
import { DownloadOutlined, ThunderboltOutlined, FileZipOutlined } from "@ant-design/icons";

const { Text } = Typography;

const ALL_AREAS = [
  { label: "DSSI", value: "DSSI" },
  { label: "TVARKOS", value: "TVARKOS" },
  { label: "AAP", value: "AAP" },
  { label: "PAREIGINIAI NUOSTATAI", value: "PAREIGINIAI NUOSTATAI" },
  { label: "PRIEDAI", value: "PRIEDAI" },
  { label: "MOKYMAI / GS", value: "MOKYMAI/GS" },
  { label: "MOKYMAI / Krovos rankomis", value: "MOKYMAI/Krovos rankomis" },
];

interface Company {
  id: number;
  type: string;
  name: string;
  code: string;
}

interface GenerationResult {
  recordId: number;
  areas: string[];
  type: "full" | "partial";
}

export const DocumentsPage = () => {
  const [companies, setCompanies] = useState<Company[]>([]);
  const [selectedCompanyId, setSelectedCompanyId] = useState<number | null>(null);
  const [selectedAreas, setSelectedAreas] = useState<string[]>([]);
  const [result, setResult] = useState<GenerationResult | null>(null);
  const [loadingFull, setLoadingFull] = useState(false);
  const [loadingPartial, setLoadingPartial] = useState(false);
  const [loadingDownload, setLoadingDownload] = useState(false);

  useEffect(() => {
    fetch("http://localhost:8080/api/companies")
      .then((res) => res.json())
      .then((data) =>
        setCompanies(
          data.map((c: any) => ({ id: c.id, type: c.type, name: c.name, code: c.code }))
        )
      )
      .catch(() =>
        notification.error({ message: "Error", description: "Failed to load companies" })
      );
  }, []);

  const handleCompanyChange = (value: number) => {
    setSelectedCompanyId(value);
    setResult(null);
    setSelectedAreas([]);
  };

  const toggleArea = (area: string) =>
    setSelectedAreas((prev) =>
      prev.includes(area) ? prev.filter((a) => a !== area) : [...prev, area]
    );

  const generateFull = async () => {
    if (!selectedCompanyId) return;
    setLoadingFull(true);
    try {
      const res = await fetch(
        `http://localhost:8080/api/documents/generate-package/company/${selectedCompanyId}`,
        { method: "POST" }
      );
      const data = await res.json();
      if (!res.ok) throw new Error(data);
      setResult({ recordId: data.generationRecordId, areas: ALL_AREAS.map((a) => a.label), type: "full" });
      notification.success({ message: "Success", description: "All documents generated successfully" });
    } catch (e: any) {
      notification.error({ message: "Error", description: e.message || "Failed to generate documents" });
    } finally {
      setLoadingFull(false);
    }
  };

  const generateByAreas = async () => {
    if (!selectedCompanyId || selectedAreas.length === 0) return;
    setLoadingPartial(true);
    try {
      const res = await fetch("http://localhost:8080/api/documents/generate-by-areas", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ companyId: selectedCompanyId, selectedAreas }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(typeof data === "string" ? data : JSON.stringify(data));
      setResult({
        recordId: data.generationRecordId,
        areas: selectedAreas.map((v) => ALL_AREAS.find((a) => a.value === v)?.label || v),
        type: "partial",
      });
      notification.success({ message: "Success", description: "Selected areas generated successfully" });
    } catch (e: any) {
      notification.error({ message: "Error", description: e.message || "Failed to generate documents" });
    } finally {
      setLoadingPartial(false);
    }
  };

  const downloadDocuments = () => {
    if (!result) return;
    setLoadingDownload(true);
    window.open(`http://localhost:8080/api/documents/download/${result.recordId}`, "_blank");
    setTimeout(() => setLoadingDownload(false), 1500);
  };

  const selectedCompany = companies.find((c) => c.id === selectedCompanyId);

  return (
    <Space direction="vertical" style={{ width: "100%" }} size="large">
      <Card title="Generate Documents">
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
          <div>
            <Text strong>Select Company</Text>
            <Select
              style={{ width: "100%", marginTop: 8 }}
              placeholder="Choose a company..."
              showSearch
              optionFilterProp="label"
              value={selectedCompanyId}
              onChange={handleCompanyChange}
              options={companies.map((c) => ({
                value: c.id,
                label: `${c.type} ${c.name} (${c.code})`,
              }))}
            />
          </div>

          {selectedCompanyId && (
            <>
              <Divider orientation="left">Generation Mode</Divider>

              <Card size="small" style={{ background: "#f6ffed", border: "1px solid #b7eb8f" }}>
                <Space style={{ width: "100%", justifyContent: "space-between" }} wrap>
                  <div>
                    <Text strong>Full Package</Text>
                    <br />
                    <Text type="secondary">
                      Generates documents for all areas based on files in the templates directory
                    </Text>
                  </div>
                  <Button
                    type="primary"
                    icon={<ThunderboltOutlined />}
                    loading={loadingFull}
                    onClick={generateFull}
                  >
                    Generate All
                  </Button>
                </Space>
              </Card>

              <Card size="small" style={{ background: "#e6f4ff", border: "1px solid #91caff" }}>
                <Text strong>Generate by Areas</Text>
                <br />
                <Text type="secondary" style={{ display: "block", marginBottom: 12 }}>
                  Select specific areas to generate documents for
                </Text>

                <Space style={{ marginBottom: 8 }}>
                  <Button size="small" onClick={() => setSelectedAreas(ALL_AREAS.map((a) => a.value))}>
                    Select all
                  </Button>
                  <Button size="small" onClick={() => setSelectedAreas([])}>
                    Clear
                  </Button>
                </Space>

                <div
                  style={{
                    display: "grid",
                    gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))",
                    gap: 8,
                    marginBottom: 16,
                  }}
                >
                  {ALL_AREAS.map((area) => (
                    <Checkbox
                      key={area.value}
                      checked={selectedAreas.includes(area.value)}
                      onChange={() => toggleArea(area.value)}
                    >
                      {area.label}
                    </Checkbox>
                  ))}
                </div>

                <Button
                  type="primary"
                  icon={<ThunderboltOutlined />}
                  loading={loadingPartial}
                  disabled={selectedAreas.length === 0}
                  onClick={generateByAreas}
                >
                  Generate Selected ({selectedAreas.length})
                </Button>
              </Card>
            </>
          )}
        </Space>
      </Card>

      {result && (
        <Card
          title={
            <Space>
              <FileZipOutlined style={{ color: "#52c41a" }} />
              <span>Documents Ready</span>
              <Tag color={result.type === "full" ? "green" : "blue"}>
                {result.type === "full" ? "Full Package" : "Partial"}
              </Tag>
            </Space>
          }
        >
          <Space direction="vertical" style={{ width: "100%" }}>
            {selectedCompany && (
              <Text>
                <Text strong>Company: </Text>
                {selectedCompany.type} {selectedCompany.name} ({selectedCompany.code})
              </Text>
            )}
            <div>
              <Text strong>Areas: </Text>
              <Space wrap style={{ marginTop: 4 }}>
                {result.areas.map((area) => (
                  <Tag key={area}>{area}</Tag>
                ))}
              </Space>
            </div>
            <Button
              type="primary"
              size="large"
              icon={<DownloadOutlined />}
              loading={loadingDownload}
              onClick={downloadDocuments}
              style={{ marginTop: 8 }}
            >
              Download ZIP
            </Button>
          </Space>
        </Card>
      )}
    </Space>
  );
};
