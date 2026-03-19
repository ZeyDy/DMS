import { Form, Card, Button, notification } from "antd";
import { useNavigate } from "react-router-dom";
import dayjs from "dayjs";
import { CompanyForm } from "../components/CompanyForm/CompanyForm";

export const CreateCustomerPage = () => {
  const [form] = Form.useForm();
  const navigate = useNavigate();

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
      const response = await fetch("http://localhost:8080/api/companies", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      await response.json();

      form.resetFields();

      notification.success({
        message: "Success",
        description: "Customer created successfully",
      });

      navigate("/customers");
    } catch (error) {
      console.error("Error creating company:", error);

      notification.error({
        message: "Error",
        description: "Failed to create customer",
      });
    }
  };

  return (
    <Card title="Create Customer" style={{ maxWidth: 900 }}>
      <Form
        form={form}
        layout="vertical"
        onFinish={onFinish}
        initialValues={{
          date: dayjs()
        }}
      >
        <CompanyForm />

        <Button type="primary" htmlType="submit">
          Create Customer
        </Button>
      </Form>
    </Card>
  );
};