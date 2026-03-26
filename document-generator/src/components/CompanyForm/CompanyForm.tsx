import { Form, Input, Select, DatePicker } from "antd"
import { Wrapper, TwoColumns, SectionTitle } from "./styles"

const { Option } = Select

export const CompanyForm = () => {
  return (
    <Wrapper>
      <SectionTitle>Company Details</SectionTitle>

      <TwoColumns>
        <Form.Item
          label="Company Type"
          name="companyType"
          rules={[{ required: true, message: "Please select company type" }]}
        >
          <Select placeholder="Select type">
            <Option value="UAB">UAB</Option>
            <Option value="MB">Small Partnership (MB)</Option>
            <Option value="II">Individual Enterprise (II)</Option>
            <Option value="AB">Joint Stock Company (AB)</Option>
            <Option value="OTHER">Other</Option>
          </Select>
        </Form.Item>

        <Form.Item
          label="Company Name"
          name="companyName"
          rules={[
            { required: true, message: "Please enter company name" },
            { min: 2, message: "Name must be at least 2 characters" }
          ]}
        >
          <Input placeholder="Enter company name" />
        </Form.Item>

        <Form.Item
          label="Company Category"
          name="companyCategory"
          rules={[{ required: true, message: "Please enter category" }]}
        >
          <Input placeholder="Enter category" />
        </Form.Item>

        <Form.Item
          label="Company Code"
          name="companyCode"
          rules={[
            { required: true, message: "Please enter company code" },
            {
              pattern: /^[0-9]+$/,
              message: "Code must contain only numbers"
            },
            {
              len: 9,
              message: "Code must be exactly 9 digits"
            }
          ]}
        >
          <Input placeholder="Enter company code" />
        </Form.Item>

        <Form.Item
          label="Address"
          name="address"
          rules={[{ required: true, message: "Please enter address" }]}
        >
          <Input placeholder="Enter address" />
        </Form.Item>

        <Form.Item
          label="City / District"
          name="city"
          rules={[{ required: true, message: "Please enter city" }]}
        >
          <Input placeholder="Enter city" />
        </Form.Item>

        <Form.Item
          label="Manager Position"
          name="managerType"
          rules={[{ required: true, message: "Please select manager position" }]}
        >
          <Select placeholder="Select position">
            <Option value="DIRECTOR">Director</Option>
            <Option value="CEO">CEO</Option>
            <Option value="MANAGER">Manager</Option>
            <Option value="OWNER">Owner</Option>
          </Select>
        </Form.Item>

        <Form.Item
          label="Manager Full Name"
          name="managerName"
          rules={[
            { required: true, message: "Please enter manager name" },
            { min: 3, message: "Name too short" }
          ]}
        >
          <Input placeholder="Enter full name" />
        </Form.Item>

        <Form.Item
          label="Document Date"
          name="date"
          rules={[{ required: true, message: "Please select date" }]}
        >
          <DatePicker style={{ width: "100%" }} />
        </Form.Item>
      </TwoColumns>
    </Wrapper>
  )
}