import { Form, Card, Button, notification } from "antd";
import { useParams, useNavigate } from "react-router-dom";
import { useEffect } from "react";
import dayjs from "dayjs";
import { CompanyForm } from "../components/CompanyForm/CompanyForm";

export const EditCustomerPage = () => {
  const [form] = Form.useForm();
  const { id } = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    fetch(`http://localhost:8080/api/companies/${id}`)
      .then((res) => res.json())
      .then((data) => {
        form.setFieldsValue({
          companyType: data.type,
          companyName: data.name,
          companyCode: data.code,
          companyCategory: data.category,
          address: data.address,
          city: data.cityOrDistrict,
          managerType: data.managerType,
          managerName: data.managerFullName,
          date: data.documentDate ? dayjs(data.documentDate) : null,
        });
      });
  }, [id]);

  const onFinish = async (values: any) => {
    const payload = {
      type: values.companyType,
      name: values.companyName,
      code: values.companyCode,
      category: values.companyCategory,
      address: values.address,
      cityOrDistrict: values.city,
      managerType: values.managerType,
      managerFullName: values.managerName,
      documentDate: values.date?.format("YYYY-MM-DD"),
    };

    try {
      await fetch(`http://localhost:8080/api/companies/${id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      notification.success({
        message: "Success",
        description: "Customer updated successfully",
      });

      navigate("/customers");
    } catch (error) {
      console.error("Error updating company:", error);

      notification.error({
        message: "Error",
        description: "Failed to update customer",
      });
    }
  };

  return (
    <Card title="Edit Customer" style={{ maxWidth: 900 }}>
      <Form form={form} layout="vertical" onFinish={onFinish}>
        <CompanyForm />

        <Button type="primary" htmlType="submit">
          Save Changes
        </Button>
      </Form>
    </Card>
  );
};