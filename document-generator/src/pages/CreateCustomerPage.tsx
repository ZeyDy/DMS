import { Form, Card } from "antd";
import { CompanyForm } from "../components/CompanyForm/CompanyForm";

export const CreateCustomerPage = () => {
  const [form] = Form.useForm();

  return (
    <Card title="Create Customer" style={{ maxWidth: 900 }}>
      <Form form={form} layout="vertical">
        <CompanyForm />
      </Form>
    </Card>
  );
};